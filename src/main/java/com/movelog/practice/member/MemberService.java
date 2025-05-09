package com.movelog.practice.member;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

@Slf4j
public class MemberService implements InitializingBean, DisposableBean, BeanNameAware {

    private final MemberRepository memberRepository;
    private String beanName;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void join(Member member) {
        log.info("[" + beanName + "] 회원 가입 시도: " + member.getName());
        memberRepository.save(member);
    }


    // MemberService의 자원 관리 확인을 위해 직접 초기화 및 종료 메서드를 구현
    // 일반적으로는 @PostConstruct와 @PreDestroy를 사용하여 초기화 및 종료 메서드를 관리

    @Override
    public void afterPropertiesSet() {
        log.info("[" + beanName + "] 초기화 완료 (afterPropertiesSet)");
    }

    @Override
    public void destroy() {
        log.info("[" + beanName + "] 리소스 정리됨 (destroy)");
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
}
