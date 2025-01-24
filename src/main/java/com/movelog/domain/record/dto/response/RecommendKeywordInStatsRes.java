package com.movelog.domain.record.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RecommendKeywordInStatsRes {
    @Schema( type = "int", example = "1", description="추천 키워드 ID")
    private Long keywordId;

    @Schema( type = "String", example ="헬스", description="추천 키워드(명사)")
    private String noun;
}
