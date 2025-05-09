package com.movelog.practice.member;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemberRepository {

    public void save(Member member) {
        log.info("회원 저장됨: " + member.getName());
    }
}