package com.movelog.domain.record.domain;

import com.movelog.domain.common.BaseEntity;
import com.movelog.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "record")
@NoArgsConstructor
@Getter
public class Record extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id", updatable = false)
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id")
    private Keyword keyword;

    @Column(name = "record_image")
    private String recordImage;

    @Column(name = "action_time")
    private java.time.LocalDateTime actionTime;

    @Builder
    public Record(Keyword keyword, String recordImage) {
        this.keyword = keyword;
        this.recordImage = recordImage;
        this.actionTime = actionTime == null? LocalDateTime.now():actionTime;
    }
}
