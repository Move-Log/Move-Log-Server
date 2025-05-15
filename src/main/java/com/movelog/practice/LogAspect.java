package com.movelog.practice;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogAspect {

    @Before("execution(* com.movelog.practice.member..*(..))")
    public void logBefore(JoinPoint joinPoint) {
        log.info("[AOP] 시작 - {}", joinPoint.getSignature().toShortString());
    }

    @Before("execution(* com.movelog.practice.member..*(..))")
    public void logAfter(JoinPoint joinPoint) {
        log.info("[AOP] 종료 - {}", joinPoint.getSignature().toShortString());
    }

    @Before("execution(* com.movelog.practice.member..*(..))")
    public void logException(JoinPoint joinPoint) {
        log.error("[AOP] 예외 발생 - {}", joinPoint.getSignature().toShortString());
    }

}
