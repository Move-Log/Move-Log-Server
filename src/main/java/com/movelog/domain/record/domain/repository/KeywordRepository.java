package com.movelog.domain.record.domain.repository;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.VerbType;
import com.movelog.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword,Long> {
    List<Keyword> findByUser(User user);

    List<Keyword> findTop5ByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndKeywordAndVerbType(User user, String noun, VerbType verbType);

    Keyword findByUserAndKeywordAndVerbType(User user, String noun, VerbType verbType);

    List<Keyword> findAllByUserAndKeywordContaining(User user, String keyword);


    // 사용자가 기록한 것 중 동일한 keyword(명사)를 가진 Keyword 리스트 조회
    @Query("SELECT k FROM Keyword k WHERE k.user.id = :userId AND k.keyword = :keyword")
    List<Keyword> findByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    // 특정 키워드 ID 목록에 해당하는 전체 기록 개수 조회
    @Query("SELECT COUNT(r) FROM Record r WHERE r.keyword.keywordId IN :keywordIds")
    int countByKeywordIds(@Param("keywordIds") List<Long> keywordIds);

    // 특정 키워드 ID 목록에 해당하는 가장 최근 기록 시간 조회
    @Query("SELECT MAX(r.actionTime) FROM Record r WHERE r.keyword.keywordId IN :keywordIds")
    Optional<LocalDateTime> findLastRecordedAtByKeywordIds(@Param("keywordIds") List<Long> keywordIds);

    // 특정 키워드 ID 목록에 해당하는 일일 평균 기록 수 계산
    @Query("SELECT COUNT(r), DATE(r.actionTime) FROM Record r WHERE r.keyword.keywordId IN :keywordIds GROUP BY DATE(r.actionTime)")
    List<Object[]> calculateAvgDailyRecordsByKeywordIds(@Param("keywordIds") List<Long> keywordIds);

    // 특정 키워드 ID 목록에 해당하는 최근 7일간 평균 기록 수 계산
    @Query("SELECT COUNT(r) FROM Record r WHERE r.keyword.keywordId IN :keywordIds AND r.actionTime >= :startDate")
    long calculateAvgWeeklyRecordsByKeywordIds(@Param("keywordIds") List<Long> keywordIds, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT MIN(k.keywordId) AS keywordId, k.keyword " +
            "FROM Keyword k " +
            "WHERE LOWER(k.keyword) LIKE LOWER(CONCAT(:keyword, '%')) " +
            "GROUP BY k.keyword " +
            "ORDER BY MIN(k.keywordId) ASC")
    List<Object[]> findAllKeywordStartingWith(@Param("keyword") String keyword);

}
