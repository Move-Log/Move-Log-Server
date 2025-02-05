package com.movelog.domain.record.application;


import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.VerbType;
import com.movelog.domain.record.domain.repository.RecordRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DataMigrationService {

    private final RecordRepository recordRepository; // 기존 DB 조회용
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 기존 DB 데이터를 Redis로 마이그레이션
     */
    @Transactional
    public void migrateDataToRedis() {
        List<Record> records = recordRepository.findAllWithKeyword(); // 모든 데이터 한 번에 조회

        for (Record record : records) {
            if (record.getKeyword() == null) continue;

            String action = record.getKeyword().getKeyword(); // 키워드 (명사)
            LocalDate date = record.getActionTime().toLocalDate(); // 날짜
            VerbType verbType = record.getKeyword().getVerbType(); // 동사 유형

            if (verbType == null) continue; // Null 체크

            // Redis 키 설정 (날짜별, 전체 통계)
            String dailyKey = "stats:daily:" + verbType.getVerbType() + ":" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String totalKey = "stats:total:" + verbType.getVerbType();

            // Redis에 데이터 저장
            redisTemplate.opsForZSet().incrementScore(dailyKey, action, 1);
            redisTemplate.opsForZSet().incrementScore(totalKey, action, 1);
        }

        System.out.println("✅ Redis 데이터 마이그레이션 완료!");
    }
}
