package com.movelog.domain.record.domain;

import com.movelog.domain.common.BaseEntity;
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

    private String keyword;

    @Enumerated(EnumType.STRING) // Enum을 문자열로 저장
    @Column(name = "verb_type")
    private VerbType verbType;

    @OneToMany(mappedBy = "keyword")
    private List<Record> records = new ArrayList<>();

    @Builder
    public Keyword(String keyword, VerbType verbType) {
        this.keyword = keyword;
        this.verbType = verbType;
    }
}
