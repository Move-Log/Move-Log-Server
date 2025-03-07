package com.movelog.domain.news.presentation;

import com.movelog.domain.news.application.NewsService;
import com.movelog.domain.news.dto.request.CreateNewsReq;
import com.movelog.domain.news.dto.request.NewsHeadLineReq;
import com.movelog.domain.news.dto.response.*;
import com.movelog.global.config.security.token.CurrentUser;
import com.movelog.global.config.security.token.UserPrincipal;
import com.movelog.global.payload.Message;
import com.movelog.global.util.ApiResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/news")
@Tag(name = "News", description = "뉴스 관련 API입니다.")
public class NewsController {

    private final NewsService newsService;

    @Operation(summary = "뉴스 헤드라인 생성 API", description = "뉴스 헤드라인을 생성하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뉴스 헤드라인 생성 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = HeadLineRes.class))),
            @ApiResponse(responseCode = "400", description = "뉴스 헤드라인 생성 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{keywordId}/headline")
    public ResponseEntity<?> createHeadLine(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "키워드 ID(동사-명사 쌍에 대한 ID)를 입력해주세요.", required = true) @PathVariable Long keywordId,
            @Parameter(description = "뉴스 헤드라인 생성 요청", required = true) @RequestBody NewsHeadLineReq newsHeadLineReq
    ) {
        List<HeadLineRes> response = newsService.createHeadLine(userPrincipal, keywordId, newsHeadLineReq);
        return ResponseEntity.ok(ApiResponseUtil.success(response));
    }


    @Operation(summary = "뉴스 생성 및 저장 API", description = "새로운 뉴스를 생성하고 저장합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뉴스 생성 및 저장 성공",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Message.class))),
            @ApiResponse(responseCode = "400", description = "뉴스 생성 및 저장 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{keywordId}")
    public ResponseEntity<?> createNews(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "키워드 ID(동사-명사 쌍에 대한 ID)를 입력해주세요.", required = true) @PathVariable Long keywordId,
            @Parameter(description = "뉴스 생성 및 저장을 위한 정보를 입력해주세요", required = true) @RequestPart CreateNewsReq createNewsReq,
            @Parameter(description = "뉴스 이미지를 파일 형식으로 입력해주세요", required = true) @RequestParam(value = "img", required = false) MultipartFile img
            ) {
        newsService.createNews(userPrincipal, keywordId, createNewsReq, img);
        return ResponseEntity.ok(ApiResponseUtil.success(Message.builder().message("뉴스가 생성되었습니다.").build()));
    }


    @Operation(summary = "뉴스 추천 키워드 조회 API", description = "뉴스 생성 시 최근 생성된 5개의 동사-명사 쌍 목록을 조회합니다. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뉴스 추천 기록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = RecentKeywordsRes.class))),
            @ApiResponse(responseCode = "400", description = "뉴스 추천 기록 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/recommend")
    public ResponseEntity<?> getRecentKeywords(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        List<RecentKeywordsRes> response = newsService.getRecentKeywords(userPrincipal);
        return ResponseEntity.ok(ApiResponseUtil.success(response));
    }

    @Operation(summary = "최근 뉴스 목록 조회 API", description = "최근 일주일간 생성한 뉴스 목록을 1페이지 당 15개씩 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최근 뉴스 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = RecentNewsRes.class))),
            @ApiResponse(responseCode = "400", description = "최근 뉴스 목록 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/week")
    public ResponseEntity<?> getRecentNews(
            @Parameter(description = "Access Token을 입력해주세요.", required = true)
                @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "뉴스 목록의 페이지 번호를 입력해주세요. **Page는 0부터 시작됩니다!**", required = true)
                @RequestParam(value = "page", required = false, defaultValue = "0") Integer page
    ) {
        Page<RecentNewsRes> response = newsService.getRecentNews(userPrincipal, page);
        return ResponseEntity.ok(ApiResponseUtil.success(response));
    }


    @Operation(summary = "뉴스 기록 현황 조회 API", description = "오늘 기준 사용자의 뉴스 기록 현황을 조회합니다. ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뉴스 기록 현황 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = TodayNewsStatusRes.class))),
            @ApiResponse(responseCode = "400", description = "뉴스 기록 현황 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/today")
    public ResponseEntity<?> getTodayNewsStatus(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        TodayNewsStatusRes response = newsService.getTodayNewsStatus(userPrincipal);
        return ResponseEntity.ok(ApiResponseUtil.success(response));
    }



    @Operation(summary = "날짜별 뉴스 목록 조회 API", description = "특정 날짜의 뉴스 목록을 1페이지 당 15개씩 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "날짜별 뉴스 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = NewsCalendarRes.class))),
            @ApiResponse(responseCode = "400", description = "날짜별 뉴스 목록 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/calendar/{date}")
    public ResponseEntity<?> getNewsByDate(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "조회할 날짜를 입력해주세요. (yyyy-MM-dd 형식)", required = true) @PathVariable String date,
            @Parameter(description = "뉴스 목록의 페이지 번호를 입력해주세요. **Page는 0부터 시작됩니다!**", required = true)
                @RequestParam(value = "page", required = false, defaultValue = "0") Integer page
    ) {
        Page<NewsCalendarRes> response = newsService.getNewsByDate(userPrincipal, date, page);
        return ResponseEntity.ok(ApiResponseUtil.success(response));
    }










}
