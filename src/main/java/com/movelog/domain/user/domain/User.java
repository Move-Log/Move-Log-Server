package com.movelog.domain.user.domain;


import com.movelog.domain.common.BaseEntity;

import com.movelog.domain.record.domain.Keyword;
import com.movelog.domain.record.domain.Record;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    private String nickname;

    private String username;

    private String email;

    private String role;

    private String provider;

    private String providerId;

    private boolean isRegistered;

    private String fcmToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Keyword> keywords = new ArrayList<>();


    @Builder
    public User(String nickname, String username, String email, String role, String provider, String providerId) {
        this.nickname = nickname;
        this.username = username;
        this.email = email;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.isRegistered = false;
    }

    public void updateIsRegistered() {
        this.isRegistered = true;
    }
}
