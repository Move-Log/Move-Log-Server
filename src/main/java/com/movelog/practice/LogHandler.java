package com.movelog.practice;


import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * LogHandler - 프록시의 부가 기능 구현 (동적 프록시 기반의 AOP 구현체)
 * - 로그를 남기고 예외를 감지
 * - 모든 메서드 실행 전/후 로깅 처리
 */

@Slf4j
public class LogHandler implements InvocationHandler {

    private final Object target;
    private final TransactionManager txManager = new TransactionManager();

    public LogHandler(Object target) {
        this.target = target;
    }

    // 프록시 객체가 호출하는 메서드
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        logBefore(methodName);      // 1. 메서드 시작 로그
        txManager.begin();          // 2. 트랜잭션 시작

        try {
            Object result = method.invoke(target, args); // 3. 실제 비즈니스 로직 실행

            txManager.commit();     // 4. 정상 종료 시 트랜잭션 커밋
            logAfter(methodName);   // 5. 정상 종료 로그
            return result;

        } catch (InvocationTargetException e) {
            return handleTargetException(methodName, e); // 6. 비즈니스 로직에서 발생한 예외 처리

        } catch (Exception e) {
            return handleUnexpectedException(methodName, e); // 7. 예기치 못한 예외 처리
        }
    }

    // 메서드 실행 전 로그 출력
    private void logBefore(String methodName) {
        log.info("[AOP] 메서드 시작: {}", methodName);
    }

    // 메서드 실행 후 정상 종료 로그 출력
    private void logAfter(String methodName) {
        log.info("[AOP] 메서드 정상 종료: {}", methodName);
    }

    // 비즈니스 로직에서 발생한 예외 처리
    private Object handleTargetException(String methodName, InvocationTargetException e) throws Throwable {
        Throwable targetEx = e.getTargetException();
        txManager.rollback(); // 트랜잭션 롤백
        log.error("[AOP] 예외 발생: {} | 메시지: {}", methodName, targetEx.getMessage(), targetEx);
        throw targetEx; // 실제 예외 다시 던짐
    }

    // 예기치 못한 예외 처리
    private Object handleUnexpectedException(String methodName, Exception e) throws Throwable {
        txManager.rollback(); // 트랜잭션 롤백
        log.error("[AOP] 예외 처리 실패: {} | 메시지: {}", methodName, e.getMessage(), e);
        throw e;
    }
}