package com.movelog.domain.record.application;


import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.VerbType;
import com.movelog.domain.record.domain.repository.RecordRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class DataMigrationService {

    private final RecordRepository recordRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 애플리케이션 실행 시 기존 데이터를 Redis로 마이그레이션
     */
    // @PostConstruct
    @Transactional
    public void migrateDataToRedis() {
        log.info("🔄 Redis 데이터 마이그레이션 시작...");

        List<Record> records = recordRepository.findAllWithKeyword();

        // 기존 순위 데이터 확인 (TOP 5 키워드 저장)
        for (VerbType verbType : VerbType.values()) {
            String category = verbType.getVerbType();
            String redisKey = "top_records_" + category;

            // Redis에 기존 순위가 없으면 초기화
            Boolean exists = redisTemplate.hasKey(redisKey);
            if (Boolean.FALSE.equals(exists)) {
                initializePreviousRankings(category, records);
            }
        }

        // 전체 기록을 Redis에 저장
        for (Record record : records) {
            if (record.getKeyword() == null) continue;

            String action = record.getKeyword().getKeyword(); // 키워드 (명사)
            LocalDate date = record.getActionTime().toLocalDate(); // 날짜
            VerbType verbType = record.getKeyword().getVerbType(); // 동사 유형

            if (verbType == null) continue; // Null 체크

            // Redis 키 설정 (날짜별, 전체 통계)
            String dailyKey = "stats:daily:" + verbType.getVerbType() + ":" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String totalKey = "stats:total:" + verbType.getVerbType();

            // Redis에 데이터 저장 (ZSet 사용: 키워드별 카운트 증가)
            redisTemplate.opsForZSet().incrementScore(dailyKey, action, 1);
            redisTemplate.opsForZSet().incrementScore(totalKey, action, 1);
        }

        log.info("✅ Redis 데이터 마이그레이션 완료! 저장된 데이터 개수: {}", records.size());
    }

    /**
     * 이전 순위 데이터를 초기화하고 Redis에 저장
     */
    private void initializePreviousRankings(String category, List<Record> records) {
        log.info("⚠️ Redis에 기존 순위 데이터가 없음. 초기화 진행: {}", category);

        Map<String, Integer> keywordCounts = new HashMap<>();

        // 해당 카테고리의 데이터를 수집하여 개수 카운트
        for (Record record : records) {
            if (record.getKeyword() != null && record.getKeyword().getVerbType().getVerbType().equals(category)) {
                String keyword = record.getKeyword().getKeyword();
                keywordCounts.put(keyword, keywordCounts.getOrDefault(keyword, 0) + 1);
            }
        }

        // TOP 5 키워드 정렬
        List<Map.Entry<String, Integer>> sortedEntries = keywordCounts.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        // Redis에 저장할 순위 데이터 생성
        Map<String, String> initialRankings = new LinkedHashMap<>();
        int rank = 1;
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            initialRankings.put(entry.getKey(), String.valueOf(rank++)); // 🚀 Integer → String 변환 후 저장
        }

        // Redis에 저장
        String redisKey = "top_records_" + category;
        redisTemplate.opsForHash().putAll(redisKey, initialRankings);
        log.info("✅ 초기 순위 저장 완료: {} → {}", redisKey, initialRankings);
    }



}
