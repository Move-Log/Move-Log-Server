package com.movelog.domain.news.domain.repository;

import com.movelog.domain.news.domain.News;
import com.movelog.domain.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    @Query("SELECT n FROM News n " +
            "JOIN n.keyword k " +
            "WHERE k.user = :user " +
            "AND n.createdAt > :createdAt " +
            "ORDER BY n.createdAt DESC")
    List<News> findRecentNewsByUser(
            @Param("user") User user,
            @Param("createdAt") LocalDateTime createdAt,
            Pageable pageable
    );
}
