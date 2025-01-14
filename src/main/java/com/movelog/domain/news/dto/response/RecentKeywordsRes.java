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
public class RecentKeywordsRes {
    @Schema( type = "int", example ="1", description="키워드 ID")
    private Long keywordId;

    @Schema( type = "String", example ="클라이밍", description="명사")
    private String noun;

    @Schema( type = "String", example ="시작하다", description="동사")
    private String verb;

}
