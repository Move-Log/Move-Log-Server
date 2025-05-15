package com.movelog.practice;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@Slf4j
public class TransactionAspect {
    // 트랜잭션 시작
    // @Before("execution(* com.movelog.practice..*(..))")
    public void beginTransaction() {
        log.info("[AOP] 트랜잭션 시작");
    }

    // 트랜잭션 커밋
    // @AfterReturning("execution(* com.movelog.practice..*(..))")
    public void commitTransaction() {
        log.info("[AOP] 트랜잭션 커밋");
    }

    // 트랜잭션 롤백
    // @AfterThrowing("execution(* com.movelog.practice..*(..))")
    public void rollbackTransaction() {
        log.info("[AOP] 트랜잭션 롤백");
    }
}
