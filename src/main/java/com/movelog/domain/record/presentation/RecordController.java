package com.movelog.domain.record.presentation;

import com.movelog.domain.record.dto.request.CreateRecordReq;
import com.movelog.domain.record.dto.response.*;
import com.movelog.domain.record.application.RecordService;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/record")
@RequiredArgsConstructor
@Slf4j
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
        // 이미지 null 체크
        log.info("img: {}", img.isEmpty());
        if(img.isEmpty()) {
            img = null;
        }
        recordService.createRecord(userPrincipal, createRecordReq, img);
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
        TodayRecordStatus result = recordService.retrieveTodayRecord(userPrincipal);
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
            @Parameter(description = "검색할 명사를 입력해주세요.", required = true) @RequestParam String keyword
            ) {
        List<SearchKeywordRes> result = recordService.searchKeyword(userPrincipal, keyword);
        return ResponseEntity.ok(ApiResponseUtil.success(result));
    }


    @Operation(summary = "날짜별 기록 목록 조회 API", description = "특정 날짜에 생성된 기록 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "날짜별 기록 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = RecordCalendarRes.class))),
            @ApiResponse(responseCode = "400", description = "날짜별 기록 목록 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))

    })
    @GetMapping("/calendar/{date}")
    public ResponseEntity<?> getRecordByDate(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "조회할 날짜를 입력해주세요. (yyyy-MM-dd 형식)", required = true) @PathVariable String date,
            @Parameter(description = "기록 목록의 페이지 번호를 입력해주세요. **Page는 0부터 시작됩니다!**", required = true)
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page
    ) {
        Page<RecordCalendarRes> response = recordService.getRecordByDate(userPrincipal, date, page);
        return ResponseEntity.ok(ApiResponseUtil.success(response));
    }


    @Operation(summary = "사용자 기록 이미지 중 최신 5개 목록 조회 API", description = "사용자가 생성한 기록 이미지 중 최신 5개 목록을 조회하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최신 5개 기록 이미지 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = Recent5RecordImagesRes.class))),
            @ApiResponse(responseCode = "400", description = "최신 5개 기록 이미지 목록 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/current")
    public ResponseEntity<?> retrieveCurrentRecordImages(
            @Parameter(description = "User의 토큰을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<Recent5RecordImagesRes> result = recordService.retrieveCurrentRecordImages(userPrincipal);
        return ResponseEntity.ok(ApiResponseUtil.success(result));
    }



}
