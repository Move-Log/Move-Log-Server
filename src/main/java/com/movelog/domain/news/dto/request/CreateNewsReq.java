package com.movelog.domain.news.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateNewsReq {

    @Schema( type = "String", example ="5년 만의 첫 도전, 무엇이 그를 움직이게 했나?", description="사용자가 선택한/생성한 헤드라인 정보입니다.")
    private String headLine;

}
