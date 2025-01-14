package com.movelog.domain.record.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RecentRecordImagesRes {

    @Schema( type = "String", example ="https://movelog.s3.ap-northeast-2.amazonaws.com/record/2021-08-01/1.jpg", description="최근 기록 이미지 URL")
    private String imageUrl;

    @Schema( type = "LocalDateTime", example ="2021-08-01T00:00:00", description="최근 기록 이미지 생성 시간")
    private LocalDateTime createdAt;

}
