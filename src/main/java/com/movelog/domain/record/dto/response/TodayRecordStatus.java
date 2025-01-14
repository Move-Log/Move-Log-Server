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
public class TodayRecordStatus {

    @Schema( type = "boolean", example ="true", description="했어요에 대한 기록이 있는지에 대한 여부")
    private boolean isDo;

    @Schema( type = "boolean", example ="false", description="먹었어요에 대한 기록이 있는지에 대한 여부")
    private boolean isEat;

    @Schema( type = "boolean", example ="true", description="갔어요에 대한 기록이 있는지에 대한 여부")
    private boolean isGo;

}
