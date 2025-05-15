package com.movelog.practice;

import com.movelog.practice.member.MemberService;
import com.movelog.practice.member.MemberServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

/**
 * ProxyTestApplication
 * - JDK 동적 프록시를 사용해 MemberService 인터페이스를 감싼 프록시 생성
 * - 인터페이스 기반이기 때문에 newProxyInstance 사용 가능
 */
@Slf4j
public class ProxyTestApplication {
    public static void main(String[] args) {
        try {
            MemberService target = new MemberServiceImpl();

            MemberService proxy = (MemberService) Proxy.newProxyInstance(
                    MemberService.class.getClassLoader(),
                    new Class[]{MemberService.class},
                    new LogHandler(target)
            );

            proxy.register("eunbeen");
            proxy.register("error");  // 의도적 예외

        } catch (Exception e) {
            log.info("[main] 처리되지 않은 예외 발생: {}", e.getMessage());
        }
    }
}

