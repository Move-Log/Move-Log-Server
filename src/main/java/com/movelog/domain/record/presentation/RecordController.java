package com.movelog.domain.record.presentation;

import com.movelog.domain.record.dto.request.CreateRecordReq;
import com.movelog.domain.record.dto.request.SearchKeywordReq;
import com.movelog.domain.record.dto.response.RecentRecordImagesRes;
import com.movelog.domain.record.dto.response.SearchKeywordRes;
import com.movelog.domain.record.dto.response.TodayRecordStatus;
import com.movelog.domain.record.service.RecordService;
import com.movelog.global.config.security.token.UserPrincipal;
import com.movelog.global.payload.Message;
import com.movelog.global.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("api/v1/record")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;
    @Operation(summary = "기록 추가 API", description = "기록을 추가하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기록 추가 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))),
            @ApiResponse(responseCode = "400", description = "기록 추가 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "기록 추가 실패(서버 에러), Request Body 내용을 확인해주세요.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<?> createRecord(
            @Parameter(description = "User의 토큰을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Schemas의 CreateRecordReq를 참고해주세요.", required = true) @RequestPart CreateRecordReq createRecordReq,
            @RequestPart(value = "img", required = false) MultipartFile img
    ) {
        recordService.createRecord(userPrincipal.getId(), createRecordReq, img);
        return ResponseEntity.ok(ApiResponseUtil.success(Message.builder().message("기록이 생성되었습니다.").build()));
    }

    @Operation(summary = "오늘 기준 기록 현황 API", description = "오늘 기준 기록 확인하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "오늘 기준 기록 현황 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = TodayRecordStatus.class))),
            @ApiResponse(responseCode = "400", description = "오늘 기준 기록 현황 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/today")
    public ResponseEntity<?> retrieveTodayRecord(
            @Parameter(description = "User의 토큰을 입력해주세요.", required = false) @AuthenticationPrincipal UserPrincipal userPrincipal
            ) {;
        TodayRecordStatus result = recordService.retrieveTodayRecord(5L);
        return ResponseEntity.ok(ApiResponseUtil.success(result));
    }

    @Operation(summary = "최근 기록 이미지 조회 API", description = "사용자가 선택한 명사-동사 쌍에 해당하는 최근 기록 이미지(5개)를 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최근 기록 이미지 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = RecentRecordImagesRes.class))),
            @ApiResponse(responseCode = "400", description = "최근 기록 이미지 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/image/{keywordId}")
    public ResponseEntity<?> retrieveRecentRecordImages(
            @Parameter(description = "User의 토큰을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "키워드 ID(동사-명사 쌍에 대한 ID)를 입력해주세요.", required = true) @PathVariable Long keywordId
    ) {
        List<RecentRecordImagesRes> result = recordService.retrieveRecentRecordImages(userPrincipal, keywordId);
        return ResponseEntity.ok(ApiResponseUtil.success(result));
    }


    @Operation(summary = "기록 내 명사 검색 API", description = "사용자가 생성한 기록 중 명사를 통해 동사-명사 쌍을 검색하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "기록 내 명사 검색 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = SearchKeywordRes.class))),
            @ApiResponse(responseCode = "400", description = "기록 내 명사 검색 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchKeyword(
            @Parameter(description = "User의 토큰을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "검색할 명사를 입력해주세요.", required = true) @RequestBody SearchKeywordReq searchKeywordReq
            ) {
        List<SearchKeywordRes> result = recordService.searchKeyword(userPrincipal, searchKeywordReq);
        return ResponseEntity.ok(ApiResponseUtil.success(result));
    }



}
