package com.movelog.practice.member;

import com.movelog.practice.SimpleBeanFactory;

public class Main {
    public static void main(String[] args) {
        SimpleBeanFactory factory = new SimpleBeanFactory("src/main/resources/appConfig.xml");

        MemberService memberService = (MemberService) factory.getBean("memberService");
        memberService.join(new Member(1L, "Eunbeen"));
    }
}
