package com.movelog.domain.record.application;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.VerbType;
import com.movelog.domain.record.dto.request.CreateRecordReq;
import com.movelog.domain.record.dto.response.*;
import com.movelog.domain.record.domain.repository.KeywordRepository;
import com.movelog.domain.record.domain.repository.RecordRepository;
import com.movelog.domain.user.application.UserService;
import com.movelog.domain.user.domain.User;
import com.movelog.domain.user.domain.repository.UserRepository;
import com.movelog.domain.user.exception.UserNotFoundException;
import com.movelog.global.config.security.token.UserPrincipal;
import com.movelog.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Collator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RecordService {
    private final RecordRepository recordRepository;
    private final UserService userService;

    private final KeywordRepository keywordRepository;
    private final S3Util s3Util;
    private final UserRepository userRepository;

    @Transactional
    public void createRecord(UserPrincipal userPrincipal, CreateRecordReq createRecordReq, MultipartFile img) {
        User user = validUserById(userPrincipal);
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);
        validateCreateRecordReq(createRecordReq);

        String recordImgUrl;
        if(img != null){
            recordImgUrl = s3Util.uploadToRecordFolder(img);
            log.info("recordImgUrl: {}", recordImgUrl);
        }
        else{
            recordImgUrl = null;
        }

        String verb = createRecordReq.getVerbType();
        try {
            VerbType verbType = VerbType.fromValue(verb);
            String noun = createRecordReq.getNoun();

            Keyword keyword;

            // 사용자의 키워드에 존재하지 않는 경우 (새로 등록)
            if (!keywordRepository.existsByUserAndKeywordAndVerbType(user, noun, verbType)) {
                keyword = Keyword.builder()
                        .user(user)
                        .keyword(noun)
                        .verbType(verbType)
                        .build();

                keywordRepository.save(keyword);
            }
            else{
                keyword = keywordRepository.findByUserAndKeywordAndVerbType(user, noun, verbType);
            }

            Record record = Record.builder()
                    .keyword(keyword)
                    .recordImage(recordImgUrl)
//                .actionTime(LocalDateTime.now())
                    .build();

            recordRepository.save(record);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid verb type: " + verb, e);
        }

    }


    public TodayRecordStatus retrieveTodayRecord(UserPrincipal userPrincipal) {
        // 유저 유효성 검사 및 조회
        User user = validUserById(userPrincipal);

        // 오늘의 시작 시간과 끝 시간 계산
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        // 유저가 소유한 키워드 가져오기
        List<Keyword> keywords = keywordRepository.findByUser(user);

        // 키워드에 연결된 기록 중 오늘 생성된 기록 가져오기
        List<Record> records = recordRepository.findByKeywordInAndActionTimeBetween(keywords, startOfDay, endOfDay);

        log.info("Retrieved Records: {}", records);

        // Keyword에서 VerbType 추출
        Set<VerbType> todayVerbTypes = records.stream()
                .map(Record::getKeyword)      // Record -> Keyword
                .map(Keyword::getVerbType)   // Keyword -> VerbType
                .collect(Collectors.toSet()); // 중복 제거

        log.info("Today VerbTypes: {}", todayVerbTypes);

        // 모든 VerbType에 대해 존재 여부를 반환
        TodayRecordStatus todayRecordStatus = TodayRecordStatus.builder()
                .isDo(verbTypeExists(todayVerbTypes, VerbType.DO))
                .isEat(verbTypeExists(todayVerbTypes, VerbType.EAT))
                .isGo(verbTypeExists(todayVerbTypes, VerbType.GO))
                        .build();

        return todayRecordStatus;

    }

    public List<RecentRecordImagesRes> retrieveRecentRecordImages(UserPrincipal userPrincipal, Long keywordId) {
        User user = validUserById(userPrincipal);
        // User user = validUserById(5L);
        Keyword keyword = validKeywordById(keywordId);
        List<Record> records = recordRepository.findTop5ByKeywordOrderByActionTimeDesc(keyword);

        return records.stream()
                .map(record -> RecentRecordImagesRes.builder()
                        .imageUrl(record.getRecordImage())
                        .createdAt(record.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

    }


    public List<SearchKeywordRes> searchKeyword(UserPrincipal userPrincipal, String keyword) {
        User user = validUserById(userPrincipal);
        // User user = validUserById(5L);
        List<Keyword> keywords = keywordRepository.findAllByUserAndKeywordContaining(user, keyword);
        keywords.forEach(k -> log.info("Keyword in DB: {}", k.getKeyword()));

        // Collator 생성
        Collator collator = Collator.getInstance(Locale.KOREA);

        // Collator를 이용한 오름차순 정렬
        List<SearchKeywordRes> sortedResults = keywords.stream()
                .map(k -> SearchKeywordRes.builder()
                        .keywordId(k.getKeywordId())
                        .noun(k.getKeyword())
                        .verb(VerbType.getStringVerbType(k.getVerbType()))
                        .build())
                .sorted((o1, o2) -> collator.compare(o1.getNoun(), o2.getNoun())) // Collator로 비교
                .collect(Collectors.toList());

        // 정렬된 명사 목록 출력
        sortedResults.forEach(r -> log.info("Sorted Noun: {}", r.getNoun()));

        return sortedResults;
    }

    public Page<RecordCalendarRes> getRecordByDate(UserPrincipal userPrincipal, String date, Integer page) {
        User user = validUserById(userPrincipal);
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);
        LocalDateTime start = LocalDateTime.parse(date + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(date + "T23:59:59");

        Pageable pageable = PageRequest.of(0, 15); // 원하는 페이지와 크기를 지정
        Page<Record> recordList = recordRepository.findRecordByUserAndCreatedAtBetween(user, start, end, pageable);

        return recordList.map(record -> RecordCalendarRes.builder()
                .recordId(record.getRecordId())
                .recordImageUrl(record.getRecordImage())
                .noun(record.getKeyword().getKeyword())
                .verb(VerbType.getStringVerbType(record.getKeyword().getVerbType()))
                .createdAt(record.getCreatedAt())
                .build());

    }

    public List<Recent5RecordImagesRes> retrieveCurrentRecordImages(UserPrincipal userPrincipal) {
        User user = validUserById(userPrincipal);
        // User user = userRepository.findById(5L).orElseThrow(UserNotFoundException::new);

        List<Record> records = recordRepository.findTop5ByKeywordUserAndRecordImageNotNullOrderByActionTimeDesc(user);

        return records.stream()
                .map(record -> Recent5RecordImagesRes.builder()
                        .imageUrl((record.getRecordImage()))
                        .build())
                .toList();
    }


    private User validUserById(UserPrincipal userPrincipal) {
        Optional<User> userOptional = userService.findById(userPrincipal.getId());
        if (userOptional.isEmpty()) { throw new UserNotFoundException(); }
        return userOptional.get();
    }

    private Keyword validKeywordById(Long keywordId) {
        Optional<Keyword> keywordOptional = keywordRepository.findById(keywordId);
        return keywordOptional.get();
    }

    private boolean verbTypeExists(Set<VerbType> todayVerbTypes, VerbType verbType) {
        return todayVerbTypes.contains(verbType);
    }

    private void validateCreateRecordReq(CreateRecordReq createRecordReq) {
        if (createRecordReq.getVerbType() == null || createRecordReq.getVerbType().isEmpty()) {
            throw new IllegalArgumentException("verbType is required.");
        }
        if (createRecordReq.getNoun() == null || createRecordReq.getNoun().isEmpty()) {
            throw new IllegalArgumentException("noun is required.");
        }
    }


}
