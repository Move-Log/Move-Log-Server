package com.movelog.domain.news.domain;

import com.movelog.domain.common.BaseEntity;
import com.movelog.domain.record.domain.Keyword;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "News")
@NoArgsConstructor
@Getter
public class News extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id", updatable = false)
    private Long newsId;

    private String headLine; //뉴스 헤드라인

    private String newsUrl; //뉴스 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id")
    private Keyword keyword;

    @Builder
    public News(String headLine, String newsUrl, Keyword keyword) {
        this.headLine = headLine;
        this.newsUrl = newsUrl;
        this.keyword = keyword;
    }



}
