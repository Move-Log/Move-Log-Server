package com.movelog.domain.record.application;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.repository.RecordRepository;
import com.movelog.domain.record.dto.response.AllUserKeywordStatsRes;
import com.movelog.domain.record.dto.response.MyKeywordStatsRes;
import com.movelog.domain.record.dto.response.RecommendKeywordInStatsRes;
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

        // 검색어를 포함한 키워드 리스트 조회
        List<Keyword> keywords = keywordRepository.findAllKeywordStartingWith(keyword);
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

    // 사용자 개인의 특정 키워드 통계 조회
    public MyKeywordStatsRes getMyKeywordStatsRes(UserPrincipal userPrincipal, Long keywordId) {
        validUserById(userPrincipal);
        Keyword keyword = validKeywordById(keywordId);
        // 사용자가 기록한 키워드가 아닐 시, 빈 배열 반환
        if (!keyword.getUser().getId().equals(userPrincipal.getId())) {
            return MyKeywordStatsRes.builder().build();
        }

        return MyKeywordStatsRes.builder()
                .noun(keyword.getKeyword())
                .count(keywordRecordCountByKeywordId(keywordId))
                .lastRecordedAt(getLastRecordedAtByKeywordId(keywordId))
                .avgDailyRecord(calculateAverageDailyRecordsByKeywordId(keywordId))
                .avgWeeklyRecord(getAvgWeeklyRecordByKeywordId(keywordId))
                .build();
    }


    // 키워드 내 기록 개수를 반환
    private int keywordRecordCountByKeywordId(Long keywordId){
        Keyword keyword = validKeywordById(keywordId);
        return keyword.getRecords().size();
    }

    // 키워드 내 기록이 많은 순서대로 정렬
    private List<Keyword> sortKeywordByRecordCount(List<Keyword> keywords) {
        return keywords.stream()
                .sorted((k1, k2) -> keywordRecordCountByKeywordId(k2.getKeywordId()) - keywordRecordCountByKeywordId(k1.getKeywordId()))
                .toList();
    }

    // 키워드의 마지막 기록 시간을 반환
    private LocalDateTime getLastRecordedAtByKeywordId(Long keywordId) {
        Record record = recordRepository.findTopByKeywordKeywordIdOrderByActionTimeDesc(keywordId);
        return record.getActionTime();
    }

    // 키워드의 일일 평균 기록 수를 반환
    public double calculateAverageDailyRecordsByKeywordId(Long keywordId) {
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
    public double getAvgWeeklyRecordByKeywordId(Long keywordId) {
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

    public List<RecommendKeywordInStatsRes> getRecommendKeywords(UserPrincipal userPrincipal) {
        User user = validUserById(userPrincipal);
        List<Keyword> keywords = keywordRepository.findTop5ByUserOrderByCreatedAtDesc(user);

        return keywords.stream()
                .map(keyword -> RecommendKeywordInStatsRes.builder()
                        .keywordId(keyword.getKeywordId())
                        .noun(keyword.getKeyword())
                        .build())
                .toList();
    }


    // 전체 사용자의 특정 키워드 통계 조회
    public AllUserKeywordStatsRes getAllUserKeywordStats(UserPrincipal userPrincipal, String keyword) {
        validUserById(userPrincipal);
        // 해당 키워드에 대한 전체 사용자의 기록 목록
        List<Record> records = recordRepository.findAllByKeyword(keyword);

        return AllUserKeywordStatsRes.builder()
                .noun(keyword)
                .count(records.size())
                .lastRecordedAt(getLastRecordedAtByRecords(records))
                .avgDailyRecord(calculateAverageDailyRecordsByRecords(keyword))
                .avgWeeklyRecord(getAvgWeeklyRecordByRecords(records))
                .build();
    }

    // 키워드의 마지막 기록 시간을 반환
    private LocalDateTime getLastRecordedAtByRecords(List<Record> records) {
        return records.stream()
                .map(Record::getActionTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    // 키워드의 일일 평균 기록 수를 반환
    private double calculateAverageDailyRecordsByRecords(String keyword) {
        /// 날짜별 기록 수 계산
        List<Object[]> results = recordRepository.findRecordCountsByKeywordGroupedByDate(keyword);

        // 총 기록 수 계산
        long totalRecords = results.stream()
                .mapToLong(row -> ((Long) row[1]))  // COUNT(r)
                .sum();

        // 기록된 날짜 수 계산
        long days = results.stream()
                .map(row -> (java.sql.Date) row[0])  // DATE(r.actionTime)
                .distinct()
                .count();

        // 일일 평균 계산
        double result = days == 0 ? 0 : (double) totalRecords / days;

        // 소수점 둘째 자리에서 반올림하여 반환
        return roundToTwoDecimal(result);
    }

    // 키워드의 최근 7일간 평균 기록 수를 반환
    private double getAvgWeeklyRecordByRecords(List<Record> records) {
        // 최근 7일간 기록 조회 (날짜 기준 오름차순 정렬 후 최근 7일 간의 기록만 추출) -> 하루에도 여러 개의 기록이 있을 수 있음
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime weekAgo = today.minusDays(7);
        List<Record> recentRecords = records.stream()
                .filter(record -> record.getActionTime().isAfter(weekAgo))
                .toList();

        // 최근 7일간 기록 수 계산
        long totalRecords = recentRecords.size();
        long days = 7;

        // 일일 평균 계산
        double result = days == 0 ? 0 : (double) totalRecords / days;
        // 소수점 둘째 자리에서 반올림하여 반환
        return roundToTwoDecimal(result);
    }


    private User validUserById(UserPrincipal userPrincipal) {
        Optional<User> userOptional = userService.findById(userPrincipal.getId());
        // Optional<User> userOptional = userRepository.findById(5L);
        if (userOptional.isEmpty()) { throw new UserNotFoundException(); }
        return userOptional.get();
    }

    private Keyword validKeywordById(Long keywordId) {
        Optional<Keyword> keywordOptional = keywordRepository.findById(keywordId);
        if(keywordOptional.isEmpty()) { throw new KeywordNotFoundException(); }
        return keywordOptional.get();
    }


}
