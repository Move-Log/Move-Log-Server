package com.movelog.practice.member;

import com.movelog.practice.LogHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("MemberService Test")
class MemberServiceTest {

    @Test
    @Nested
    @DisplayName("[성공] 회원가입 테스트")
    void testWithProxy() {
        MemberService target = new MemberServiceImpl();

        MemberService proxy = (MemberService) Proxy.newProxyInstance(
                MemberService.class.getClassLoader(),
                new Class[]{MemberService.class},
                new LogHandler(target)
        );

        proxy.register("eunbeen");
    }

    @Test
    @DisplayName("[실패] 회원가입 테스트 - 예외 검증")
    void testWithProxyError() {
        MemberService target = new MemberServiceImpl();
        MemberService proxy = (MemberService) Proxy.newProxyInstance(
                MemberService.class.getClassLoader(),
                new Class[]{MemberService.class},
                new LogHandler(target)
        );

        // 예외 발생 검증
        assertThrows(IllegalArgumentException.class, () -> proxy.register("error"));
    }
}