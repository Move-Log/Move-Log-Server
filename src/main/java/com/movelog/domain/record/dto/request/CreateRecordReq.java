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
public class CreateRecordReq {
    @Schema(type = "String", example = "했어요", description = "했어요, 먹었어요, 갔어요")
    private String verbType;

    @Schema(type = "String", example = "헬스", description = "명사 작성")
    private String noun;
}
