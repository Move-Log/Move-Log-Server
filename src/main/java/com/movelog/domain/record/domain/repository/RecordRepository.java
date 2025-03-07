package com.movelog.domain.record.domain.repository;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.VerbType;
import com.movelog.domain.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordRepository extends JpaRepository<Record,Long> {

    List<Record> findByKeywordInAndActionTimeBetween(List<Keyword> keywords, LocalDateTime startTime, LocalDateTime endTime);

    List<Record> findTop5ByKeywordOrderByActionTimeDesc(Keyword keyword);

    @Query("SELECT r FROM Record r " +
            "JOIN r.keyword k " +
            "WHERE k.user = :user " +
            "AND r.actionTime BETWEEN :start AND :end " +
            "ORDER BY r.actionTime ASC")
    Page<Record> findRecordByUserAndCreatedAtBetween(User user, LocalDateTime start, LocalDateTime end, Pageable pageable);


    // 사용자가 등록한 기록 중 가장 최근 5개의 기록을 조회, 이미지가 있는 경우만 조회
    // 5개의 기록만 조회
    List<Record> findTop5ByKeywordUserAndRecordImageNotNullOrderByActionTimeDesc(User user);

    @Query("SELECT COUNT(r) AS recordCount, DATE(r.actionTime) AS recordDate " +
            "FROM Record r " +
            "WHERE r.keyword.keywordId = :keywordId " +
            "GROUP BY DATE(r.actionTime)")
    List<Object[]> findKeywordRecordCountsByDate(Long keywordId);

    Record findTopByKeywordKeywordIdOrderByActionTimeDesc(Long keywordId);

    @Query("SELECT r FROM Record r " +
            "JOIN r.keyword k " +
            "WHERE k.keyword = :keyword " +
            "ORDER BY r.actionTime DESC")
    List<Record> findAllByKeyword(String keyword);

    @Query("SELECT DATE(r.actionTime), COUNT(r) " +
            "FROM Record r " +
            "JOIN r.keyword k " +
            "WHERE k.keyword = :keyword " +
            "GROUP BY DATE(r.actionTime)")
    List<Object[]> findRecordCountsByKeywordGroupedByDate(String keyword);

    /**
     * 모든 Record와 연관된 Keyword를 페치 조인하여 가져옴
     */
    @Query("SELECT r FROM Record r JOIN FETCH r.keyword k")
    List<Record> findAllWithKeyword();

    @Query("SELECT r FROM Record r WHERE r.keyword.verbType = :verbType " +
            "AND r.actionTime BETWEEN :startDate AND :endDate")
    List<Record> findRecordsByMonth(@Param("verbType") VerbType verbType,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);


    @Query("""
    SELECT r.actionTime
    FROM Record r
    WHERE r.keyword.keyword = :noun AND r.keyword.verbType = :verbType AND r.keyword.user.id = :userId
    ORDER BY r.actionTime DESC
    LIMIT 2
    """)
    List<LocalDateTime> findLatestTwoRecords(@Param("userId") Long userId, @Param("noun") String noun, @Param("verbType") VerbType verbType);


    @Query("""
    SELECT r.actionTime
    FROM Record r
    WHERE r.keyword.keyword = :noun AND r.keyword.verbType = :verbType AND r.keyword.user.id = :userId
    ORDER BY r.actionTime DESC
    LIMIT 1
    """)
    Optional<LocalDateTime> findLastRecordedAt(@Param("userId") Long userId, @Param("noun") String noun, @Param("verbType") VerbType verbType);

    @Query(value = """
    WITH StreakData AS (
        SELECT 
            DATE(action_time) AS record_date,
            DATE_SUB(DATE(action_time), INTERVAL ROW_NUMBER() OVER (ORDER BY action_time) DAY) AS streak_group
        FROM record
        WHERE keyword_id IN (
            SELECT k.keyword_id 
            FROM keyword k
            WHERE k.keyword = :noun 
            AND k.verb_type = :verbType
            AND k.user_id = :userId
        )
    )
    SELECT COUNT(*)
    FROM (SELECT DISTINCT record_date FROM StreakData GROUP BY streak_group) AS StreakGroups
    """, nativeQuery = true)
    int findMaxStreakDays(@Param("userId") Long userId, @Param("noun") String noun, @Param("verbType") String verbType);


}
