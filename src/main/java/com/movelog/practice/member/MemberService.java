package com.movelog.practice.member;

/**
 * MemberService 인터페이스
 * - JDK 동적 프록시는 반드시 인터페이스 기반이어야 함
 * - 프록시 객체는 인터페이스를 구현한 객체로 생성됨
 */
public interface MemberService {
    void register(String name);
}
