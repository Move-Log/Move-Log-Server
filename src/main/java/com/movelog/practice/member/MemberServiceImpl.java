package com.movelog.practice.member;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * MemberServiceImpl 클래스
 * - 실질적인 핵심 로직이 구현된 클래스
 * - 프록시가 감쌀 대상 (Target)
 */
@Slf4j
@Service
public class MemberServiceImpl implements MemberService {
    @Override
    public void register(String name) {
        log.info("회원 등록 로직 실행: {}" , name);
        // 회원 등록 로직
        if ("error".equals(name)) {
            throw new IllegalArgumentException("잘못된 이름입니다");
        }
    }
}
