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
public class RecordCalendarRes {

    @Schema( type = "int", example ="1", description="기록 ID")
    private Long recordId;

    @Schema( type = "String", example ="https://movelog.s3.ap-northeast-2.amazonaws.com/record/2021-08-01/1.jpg", description="기록 이미지 URL")
    private String recordImageUrl;

    @Schema( type = "String", example ="헬스", description="명사")
    private String noun;

    @Schema( type = "String", example ="했어요", description="동사")
    private String verb;

    @Schema( type = "LocalDateTime", example ="2025-08-01T00:00:00", description="기록 생성 시간")
    private LocalDateTime createdAt;

}
