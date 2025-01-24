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
public class Recent5RecordImagesRes {

    @Schema( type = "String", example ="https://movelog.com/record/1/image/1", description="최근 5개의 기록 이미지 URL")
    private String imageUrl;
}
