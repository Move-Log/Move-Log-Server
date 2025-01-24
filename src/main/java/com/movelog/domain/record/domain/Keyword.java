package com.movelog.domain.record.domain;

import com.movelog.domain.common.BaseEntity;
import com.movelog.domain.news.domain.News;
import com.movelog.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Keyword")
@NoArgsConstructor
@Getter
public class Keyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id", updatable = false)
    private Long keywordId;

    private String keyword; //명사

    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
    @Column(name = "verb_type")
    private VerbType verbType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Record> records = new ArrayList<>();

    @OneToMany(mappedBy = "keyword", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<News> news = new ArrayList<>();

    @Builder
    public Keyword(User user, String keyword, VerbType verbType) {
        this.user = user;
        this.keyword = keyword;
        this.verbType = verbType;
    }

}
