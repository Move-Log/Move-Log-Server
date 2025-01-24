package com.movelog.domain.record.domain.repository;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import com.movelog.domain.record.domain.VerbType;
import com.movelog.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword,Long> {
    List<Keyword> findByUser(User user);

    List<Keyword> findTop5ByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndKeywordAndVerbType(User user, String noun, VerbType verbType);

    Keyword findByUserAndKeywordAndVerbType(User user, String noun, VerbType verbType);

    List<Keyword> findAllByUserAndKeywordContaining(User user, String keyword);

}
