package com.movelog.practice.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DisplayName("MemberController Test")
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("[성공] 회원가입 요청")
    void joinSuccessTest() throws Exception {
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"eunbeen\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("회원 등록 완료: eunbeen"));
    }

    @Test
    @DisplayName("[실패] 회원가입 요청 - 잘못된 이름")
    void joinFailTest() throws Exception {
        mockMvc.perform(post("/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"error\"}"))
                .andExpect(status().isBadRequest());
    }
}
