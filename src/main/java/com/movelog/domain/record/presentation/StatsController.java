package com.movelog.domain.record.presentation;

import com.movelog.domain.record.application.KeywordService;
import com.movelog.domain.record.application.RecordService;
import com.movelog.domain.record.dto.response.*;
import com.movelog.global.config.security.token.UserPrincipal;
import com.movelog.global.payload.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stats")
@Tag(name = "Stats", description = "통계 관련 API입니다.")
public class StatsController {

    private final KeywordService keywordService;
    private final RecordService recordService;

    @Operation(summary = "통계 조회 시 단어 검색 API", description = "통계 조회 시 서비스 내에서 생성된 단어를 검색하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "단어 검색 결과 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = SearchKeywordInStatsRes.class))),
            @ApiResponse(responseCode = "400", description = "단어 검색 결과 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/word/search")
    public ResponseEntity<?> searchKeywordInStats(
        @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Parameter(description = "검색할 명사를 입력해주세요.", required = true) @RequestParam String keyword
        ) {
        List<SearchKeywordInStatsRes> response = keywordService.searchKeywordInStats(userPrincipal, keyword);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "나의 특정 단어 통계 정보 조회 API", description = "나의 특정 단어 통계 정보를 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "나의 특정 단어 통계 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = MyKeywordStatsRes.class))),
            @ApiResponse(responseCode = "400", description = "나의 특정 단어 통계 정보 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/word/my/{keywordId}")
    public ResponseEntity<?> getMyKeywordStats(
        @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Parameter(description = "검색할 명사의 id를 입력해주세요.", required = true) @PathVariable Long keywordId
        ) {
        MyKeywordStatsRes response = keywordService.getMyKeywordStatsRes(userPrincipal, keywordId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "통계 추천 단어 조회 API", description = "사용자가 최근 기록한 단어 목록(최대 5개)을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "통계 추천 단어 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = RecommendKeywordInStatsRes.class))),
            @ApiResponse(responseCode = "400", description = "통계 추천 단어 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/word/recommend")
    public ResponseEntity<?> getRecommendKeywords(
        @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal
        ) {
        List<RecommendKeywordInStatsRes> response = keywordService.getRecommendKeywords(userPrincipal);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전체 사용자 대상 특정 단어 통계 조회 API", description = "전체 사용자 데이터를 대상으로 특정 단어 통계를 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 사용자 대상 특정 단어 통계 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AllUserKeywordStatsRes.class))),
            @ApiResponse(responseCode = "400", description = "전체 사용자 대상 특정 단어 통계 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/word/all")
    public ResponseEntity<?> getAllUserKeywordStats(
        @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Parameter(description = "검색할 명사를 입력해주세요.", required = true) @RequestParam String keyword
        ) {
        AllUserKeywordStatsRes response = keywordService.getAllUserKeywordStats(userPrincipal, keyword);
        return ResponseEntity.ok(response);
    }


    @Operation(summary = "전체 사용자 대상 기록 통계 정보 조회 API",
            description = "전체 사용자 데이터를 대상으로 기록 통계 정보를 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 사용자 대상 기록 통계 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AllUserRecordStatsRes.class))),
            @ApiResponse(responseCode = "400", description = "전체 사용자 대상 기록 통계 조회 실패",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/record/all")
    public ResponseEntity<?> getAllUserRecordStats(
            @Parameter(description = "Access Token을 입력해주세요.", required = true)
            @AuthenticationPrincipal UserPrincipal userPrincipal,

            @Parameter(description = "카테고리를 입력해주세요. (했어요, 갔어요, 먹었어요)", required = true)
            @RequestParam String category,

            @Parameter(description = "조회할 기간을 입력해주세요. (daily, weekly, monthly, total)", required = true)
            @RequestParam String period,

            @Parameter(description = "월별 조회 시 필요한 연-월 값 (예: 2025-02). period=monthly일 때 필수")
            @RequestParam(required = false) String month
    ) {
        // Redis에서 통계 데이터 조회 (월별 조회 처리 추가)
        AllUserRecordStatsRes response = recordService.getAllUserRecordStats(userPrincipal, category, period, month);
        return ResponseEntity.ok(response);
    }


}
