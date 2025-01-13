package com.movelog.domain.record.repository;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecordRepository extends JpaRepository<Record,Long> {
    List<Record> findByKeywordInAndActionTimeBetween(List<Keyword> keywords, LocalDateTime startTime, LocalDateTime endTime);
}
