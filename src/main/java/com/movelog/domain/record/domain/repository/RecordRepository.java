package com.movelog.domain.record.domain.repository;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

}
