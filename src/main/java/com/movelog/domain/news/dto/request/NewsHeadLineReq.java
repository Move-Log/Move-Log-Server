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
public class NewsHeadLineReq {
    @Schema( type = "String", example ="첫 도전, 오랜만에 다시, 꾸준히 이어온 기록, 끊어낸 습관 중 택 1", description="뉴스 헤드라인 고정 옵션입니다.")
    private String option;
}
