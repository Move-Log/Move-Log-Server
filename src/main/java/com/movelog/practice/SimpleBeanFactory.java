package com.movelog.practice;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

// SimpleBeanFactory
// XML 파일을 읽어 Bean을 생성하고 관리하는 간단한 IoC 컨테이너

// DI 컨테이너의 생명주기
// 1. XML 파일을 읽어 Bean 정의를 파싱
// 2. Bean 인스턴스를 생성
// 3. 의존성 주입을 통해 Bean을 연결
// 4. 컨테이너 등록 - Bean을 Map에 저장
// 5. Bean 사용 - Bean을 요청할 때마다 Map에서 꺼내서 반환
// 6. 컨테이너 종료 - Bean을 소멸시키고 자원 해제

public class SimpleBeanFactory {

    private final Map<String, Object> beanMap = new HashMap<>(); // Bean 인스턴스 저장
    private final Map<String, Class<?>> classMap = new HashMap<>(); // Bean 클래스 저장

    // XML 경로를 받아 BeanFactory 초기화
    public SimpleBeanFactory(String xmlPath) {
        try {
            Document doc = parseXml(xmlPath); //  XML 경로 파싱
            NodeList beans = doc.getElementsByTagName("bean"); // Bean 노드 리스트 가져오기

            preloadBeanClasses(beans);        // Bean 클래스 미리 로드
            createBeanInstances(beans);       // Bean 인스턴스 생성 및 주입
            registerShutdownHook();           // 소멸 콜백 등록

        } catch (Exception e) {
            throw new RuntimeException("BeanFactory 초기화 실패", e);
        }
    }

    // Bean을 요청할 때 사용
    public Object getBean(String id) {
        return beanMap.get(id);
    }

    // 1. XML 파서
    private Document parseXml(String xmlPath) throws Exception {
        return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new File(xmlPath));
    }

    // 2. Bean 정의의 클래스 정보를 먼저 등록
    private void preloadBeanClasses(NodeList beans) throws Exception {
        for (int i = 0; i < beans.getLength(); i++) {
            Element bean = (Element) beans.item(i);
            String id = bean.getAttribute("id");
            String className = bean.getAttribute("class");
            classMap.put(id, Class.forName(className));
        }
    }

    // 3. Bean 인스턴스를 생성하고 의존성 주입
    private void createBeanInstances(NodeList beans) throws Exception {
        for (int i = 0; i < beans.getLength(); i++) {
            Element bean = (Element) beans.item(i);
            String id = bean.getAttribute("id");

            if (beanMap.containsKey(id)) continue;

            Object instance = createBeanInstance(bean);
            initializeLifecycle(id, instance);
            beanMap.put(id, instance);
        }
    }

    // 개별 Bean 인스턴스 생성 및 constructor-arg 주입
    private Object createBeanInstance(Element bean) throws Exception {
        String id = bean.getAttribute("id");
        Class<?> clazz = classMap.get(id);
        NodeList args = bean.getElementsByTagName("constructor-arg");

        if (args.getLength() == 0) {
            return clazz.getDeclaredConstructor().newInstance();
        }

        // 의존성 주입
        String ref = ((Element) args.item(0)).getAttribute("ref");
        Object dependency = beanMap.get(ref);
        if (dependency == null) {
            // 의존 Bean 먼저 생성
            Element refBean = findBeanById(bean.getOwnerDocument(), ref); // ref Bean 찾기
            dependency = createBeanInstance(refBean);
            initializeLifecycle(ref, dependency);
            beanMap.put(ref, dependency);
        }

        Constructor<?> constructor = clazz.getConstructor(dependency.getClass());
        return constructor.newInstance(dependency);
    }

    // 초기화 로직 처리
    private void initializeLifecycle(String id, Object instance) {
        if (instance instanceof BeanNameAware aware) {
            aware.setBeanName(id);
        }
        if (instance instanceof InitializingBean init) {
            try {
                init.afterPropertiesSet();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    // 소멸 콜백 처리
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("🧹 애플리케이션 종료됨, destroy() 실행");
            for (Object bean : beanMap.values()) {
                if (bean instanceof DisposableBean disposable) {
                    try {
                        disposable.destroy();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
    }
 // ---
    // XML 내 특정 id의 bean 노드 찾기
    private Element findBeanById(Document doc, String id) {
        NodeList allBeans = doc.getElementsByTagName("bean");
        for (int i = 0; i < allBeans.getLength(); i++) {
            Element bean = (Element) allBeans.item(i);
            if (bean.getAttribute("id").equals(id)) {
                return bean;
            }
        }
        throw new RuntimeException("참조된 bean을 찾을 수 없음: " + id);
    }
}
