package com.movelog.domain.record.service;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.VerbType;
import com.movelog.domain.record.dto.request.CreateRecordReq;
import com.movelog.domain.record.repository.KeywordRepository;
import com.movelog.domain.record.repository.RecordRepository;
import com.movelog.domain.user.domain.User;
import com.movelog.domain.user.domain.repository.UserRepository;
import com.movelog.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;
    private final S3Util s3Util;

    @Transactional
    public void createRecord(Long userId, CreateRecordReq createRecordReq, MultipartFile img) {
        User user = validUserById(userId);
        // User user = validUserById(5L);
        String recordImgUrl = s3Util.uploadToRecordFolder(img);
        log.info("recordImgUrl: {}", recordImgUrl);

        String verb = createRecordReq.getVerbType();
        try {
            VerbType verbType = VerbType.fromValue(verb);

            Keyword keyword = Keyword.builder()
                    .user(user)
                    .keyword(createRecordReq.getNoun())
                    .verbType(verbType)
                    .build();

            keywordRepository.save(keyword);

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

    private User validUserById(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        return userOptional.get();
    }

    public Map<String, Boolean> retrieveTodayRecord(Long userId) {
        // 유저 유효성 검사 및 조회
        User user = validUserById(userId);

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
        return Arrays.stream(VerbType.values())
                .collect(Collectors.toMap(
                        VerbType::getVerbType,         // 키: VerbType의 문자열 값
                        todayVerbTypes::contains       // 값: 오늘 VerbType에 포함 여부
                ));
    }



}
