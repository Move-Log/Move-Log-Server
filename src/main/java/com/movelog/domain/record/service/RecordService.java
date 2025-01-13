package com.movelog.domain.record.service;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.VerbType;
import com.movelog.domain.record.dto.request.CreateRecordReq;
import com.movelog.domain.record.repository.KeywordRepository;
import com.movelog.domain.record.repository.RecordRepository;
import com.movelog.domain.user.domain.User;
import com.movelog.domain.user.domain.repository.UserRepository;
import com.movelog.global.DefaultAssert;
import com.movelog.global.util.S3Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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
        String recordImgUrl = s3Util.upload(img);
        log.info("recordImgUrl: {}", recordImgUrl);

        Keyword keyword = Keyword.builder()
                .keyword(createRecordReq.getNoun())
                .build();

        keywordRepository.save(keyword);
        String verb = createRecordReq.getVerbType();
        try {
            VerbType verbType = VerbType.fromValue(verb);

            Record record = Record.builder()
                    .user(user)
                    .keyword(keyword)
                    .verbType(verbType)
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

    public List<VerbType> retrieveTodayRecord(Long userId) {
        User user = validUserById(userId);
        // 현재 날짜 가져오기
        LocalDate today = LocalDate.now(); // 오늘 날짜 (2025-01-05 기준)

        // 오늘의 시작 시간과 끝 시간 계산
        LocalDateTime startOfDay = today.atStartOfDay(); // 2025-01-05T00:00:00
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX); // 2025-01-05T23:59:59.999999999

        // 레코드 조회
        List<Record> records = recordRepository.findByUserAndActionTimeBetween(user, startOfDay, endOfDay);

        return records.stream()
                .map(Record::getVerbType) // Record 객체에서 verbType 추출
                .distinct()              // 중복 제거
                .collect(Collectors.toList());
    }
}
