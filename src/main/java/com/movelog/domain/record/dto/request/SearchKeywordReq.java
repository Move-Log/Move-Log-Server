package com.movelog.domain.record.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SearchKeywordReq {
    @Schema( type = "String", example ="헬스", description="검색할 명사")
    private String searchKeyword;
}
