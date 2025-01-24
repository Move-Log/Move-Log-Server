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
public class MyKeywordStatsRes {

    @Schema( type = "String", example = "헬스", description = "통계 대상 명사(키워드)")
    private String noun;

    @Schema( type = "int", example = "1", description = "사용자가 해당 명사에 대해 기록한 횟수")
    private int count;

    @Schema(type = "LocalDateTime", example = "2025-08-01T00:00:00", description = "마지막 기록 일시(가장 최근에 기록한 시간)")
    private LocalDateTime lastRecordedAt;

    @Schema(type = "Double", example = "0.5", description = "평균 일간 기록")
    private double avgDailyRecord;

    @Schema(type = "Double", example = "0.5", description = "최근 7일단 평균 기록")
    private double avgWeeklyRecord;

}
