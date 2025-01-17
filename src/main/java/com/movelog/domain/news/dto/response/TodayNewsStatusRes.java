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
public class TodayNewsStatusRes {

    @Schema( type = "int", example ="0 ~ 5", description = "오늘 기준 뉴스 현황입니다. 0~5 사이의 값입니다.")
    private int newsStatus;

}
