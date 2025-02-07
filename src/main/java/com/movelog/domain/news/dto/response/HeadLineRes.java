package com.movelog.domain.news.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class HeadLineRes {
    @Schema( type = "String", example ="5년 만의 첫 도전, 무엇이 그를 움직이게 했나?", description="뉴스 헤드라인 추천 내용입니다.")
    private String headLine;

    public void setHeadLine(String trim) {

    }
}
