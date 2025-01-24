package com.movelog.domain.record.application;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.repository.RecordRepository;
import com.movelog.domain.record.dto.response.MyKeywordStatsRes;
import com.movelog.domain.record.dto.response.SearchKeywordInStatsRes;
import com.movelog.domain.record.exception.KeywordNotFoundException;
import com.movelog.domain.record.domain.repository.KeywordRepository;
import com.movelog.domain.user.application.UserService;
import com.movelog.domain.user.domain.User;
import com.movelog.domain.user.domain.repository.UserRepository;
import com.movelog.domain.user.exception.UserNotFoundException;
import com.movelog.global.config.security.token.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class KeywordService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final KeywordRepository keywordRepository;
    private final RecordRepository recordRepository;

    public List<SearchKeywordInStatsRes> searchKeywordInStats(UserPrincipal userPrincipal, String keyword) {

        validUserById(userPrincipal);

        // 검색어 전처리
        String processedKeyword = keyword.trim();

        // 검색어를 포함한 키워드 리스트 조회
        List<Keyword> keywords = keywordRepository.findAllKeywordStartingWith(processedKeyword);
        log.info("Searching for keywords starting with: {}", keyword);

        // 기록이 많은 순서대로 정렬
        keywords = sortKeywordByRecordCount(keywords);

        return keywords.stream()
                .map(keyword1 -> SearchKeywordInStatsRes.builder()
                        .keywordId(keyword1.getKeywordId())
                        .noun(keyword1.getKeyword())
                        .build())
                .toList();

    }

    public MyKeywordStatsRes getMyKeywordStatsRes(UserPrincipal userPrincipal, Long keywordId) {
        validUserById(userPrincipal);
        Keyword keyword = validKeywordById(keywordId);

        return MyKeywordStatsRes.builder()
                .noun(keyword.getKeyword())
                .count(keywordRecordCount(keywordId))
                .lastRecordedAt(getLastRecordedAt(keywordId))
                .avgDailyRecord(calculateAverageDailyRecords(keywordId))
                .avgWeeklyRecord(getAvgWeeklyRecord(keywordId))
                .build();
    }


    // 키워드 내 기록 개수를 반환
    private int keywordRecordCount(Long keywordId){
        Keyword keyword = validKeywordById(keywordId);
        return keyword.getRecords().size();
    }

    // 키워드 내 기록이 많은 순서대로 정렬
    private List<Keyword> sortKeywordByRecordCount(List<Keyword> keywords) {
        return keywords.stream()
                .sorted((k1, k2) -> keywordRecordCount(k2.getKeywordId()) - keywordRecordCount(k1.getKeywordId()))
                .toList();
    }

    // 키워드의 마지막 기록 시간을 반환
    private LocalDateTime getLastRecordedAt(Long keywordId) {
        Record record = recordRepository.findTopByKeywordKeywordIdOrderByActionTimeDesc(keywordId);
        return record.getActionTime();
    }

    // 키워드의 일일 평균 기록 수를 반환
    public double calculateAverageDailyRecords(Long keywordId) {
        List<Object[]> results = recordRepository.findKeywordRecordCountsByDate(keywordId);

        // 총 기록 수와 기록된 날짜 수 계산
        long totalRecords = results.stream()
                .mapToLong(row -> (Long) row[0])  // recordCount
                .sum();

        long days = results.size();  // 날짜 수

        // 일일 평균 계산
        double result = days == 0 ? 0 : (double) totalRecords / days;
        // 소수점 둘째 자리에서 반올림하여 반환
        return roundToTwoDecimal(result);
    }

    // 키워드의 최근 7일간 평균 기록 수를 반환
    public double getAvgWeeklyRecord(Long keywordId) {
        Keyword keyword = validKeywordById(keywordId);
        List<Record> records = recordRepository.findTop5ByKeywordOrderByActionTimeDesc(keyword);

        // 최근 7일간 기록 수 계산
        long totalRecords = records.size();
        long days = 7;

        // 일일 평균 계산
        double result = days == 0 ? 0 : (double) totalRecords / days;
        // 소수점 둘째 자리에서 반올림하여 반환
        return roundToTwoDecimal(result);

    }

    // 소수점 둘째 자리에서 반올림하여 반환
    private double roundToTwoDecimal(double value) {
        return Math.round(value * 100) / 100.0;
    }

    private User validUserById(UserPrincipal userPrincipal) {
        // Optional<User> userOptional = userService.findById(userPrincipal.getId());
        Optional<User> userOptional = userRepository.findById(5L);
        if (userOptional.isEmpty()) { throw new UserNotFoundException(); }
        return userOptional.get();
    }

    private Keyword validKeywordById(Long keywordId) {
        Optional<Keyword> keywordOptional = keywordRepository.findById(keywordId);
        if(keywordOptional.isEmpty()) { throw new KeywordNotFoundException(); }
        return keywordOptional.get();
    }

}
