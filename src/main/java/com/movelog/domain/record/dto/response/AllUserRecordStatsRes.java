package com.movelog.domain.record.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AllUserRecordStatsRes {

    @Schema(type = "String", example = "했어요", description = "기록 카테고리(했어요/갔어요/먹었어요)")
    private String category;

    @Schema(type = "int", example = "500", description = "전체 사용자 총 기록 생성 건수")
    private int totalRecords;

    @Schema(type = "int", example = "30", description = "사용자들이 연속으로 기록한 최고 일 수")
    private int maxConsecutiveDays;

    @Schema(type = "double", example = "2.5", description = "전체 사용자의 평균 일간 기록")
    private double avgDailyRecord;

    @Schema(type = "int", example = "10", description = "전체 사용자 중 하루 동안 가장 많이 기록한 횟수")
    private int maxDailyRecord;

    @Schema(type = "List", description = "TOP 5 많이 기록된 키워드 목록")
    private List<Map<String, Object>> topRecords;

    @Schema(type = "Map", description = "날짜별 기록 개수 (달력에 사용)")
    private Map<LocalDate, Integer> dailyRecords;

}