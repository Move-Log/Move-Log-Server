package com.movelog.practice;

import lombok.extern.slf4j.Slf4j;

/**
 * TransactionManager - 트랜잭션 관리 클래스
 * - 트랜잭션 시작, 커밋, 롤백 기능을 제공
 * - AOP를 통해 트랜잭션 관리 기능을 추가할 수 있음
 */

//
@Slf4j
public class TransactionHandler {
    public void beginTransaction() {
        log.info("[TX] 트랜잭션 시작");
    }

    public void commitTransaction() {
        log.info("[TX] 트랜잭션 커밋");
    }

    public void rollbackTransaction() {
        log.info("[TX] 트랜잭션 롤백");
    }
}
