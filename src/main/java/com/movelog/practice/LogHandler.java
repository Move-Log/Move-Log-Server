package com.movelog.practice;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * LogHandler - 프록시의 부가 기능 구현 (동적 프록시 기반의 AOP 구현체)
 * - 로그를 남기고 예외를 감지
 * - 모든 메서드 실행 전/후 로깅 처리
 */


public class LogHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(LogHandler.class); // SLF4J Logger

    private final Object target;
    private final TransactionManager txManager = new TransactionManager();

    public LogHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        logBefore(methodName);       // [1] 메서드 시작 로그
        txManager.begin();           // [2] 트랜잭션 시작

        try {
            Object result = method.invoke(target, args); // [3] 실제 비즈니스 로직 실행
            txManager.commit();       // [4] 성공 시 커밋
            logAfter(methodName);     // [5] 성공 로그 출력
            return result;

        } catch (InvocationTargetException e) {
            return handleTargetException(methodName, e); // [6] 내부 비즈니스 예외 처리

        } catch (Exception e) {
            return handleUnexpectedException(methodName, e); // [7] 리플렉션 자체 예외 처리
        }
    }

    // [1] 메서드 실행 전 로그
    private void logBefore(String methodName) {
        log.info("[AOP] 메서드 시작: {}", methodName);
    }

    // [5] 메서드 정상 종료 로그
    private void logAfter(String methodName) {
        log.info("[AOP] 메서드 정상 종료: {}", methodName);
    }

    // [6] 비즈니스 로직에서 발생한 예외 처리
    private Object handleTargetException(String methodName, InvocationTargetException e) throws Throwable {
        Throwable targetEx = e.getTargetException();
        txManager.rollback();
        log.error("[AOP] 예외 발생: {} | 메시지: {}", methodName, targetEx.getMessage(), targetEx);
        throw targetEx;
    }

    // [7] 예기치 못한 예외 처리 (ex. method.invoke 자체 실패)
    private Object handleUnexpectedException(String methodName, Exception e) throws Throwable {
        txManager.rollback();
        log.error("[AOP] 예외 처리 실패: {} | 메시지: {}", methodName, e.getMessage(), e);
        throw e;
    }
}