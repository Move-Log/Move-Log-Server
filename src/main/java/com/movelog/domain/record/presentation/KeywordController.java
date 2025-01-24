package com.movelog.domain.record.presentation;

import com.movelog.domain.record.application.KeywordService;
import com.movelog.domain.record.dto.response.SearchKeywordInStatsRes;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/keyword")
@Tag(name = "Keyword", description = "단어 관련 API입니다.")
public class KeywordController {

    private final KeywordService keywordService;

    @Operation(summary = "통계 조회 시 단어 검색 API", description = "통계 조회 시 서비스 내에서 생성된 단어를 검색하는 API입니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "단어 검색 결과 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "array", implementation = SearchKeywordInStatsRes.class))),
            @ApiResponse(responseCode = "400", description = "단어 검색 결과 조회 실패",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/stats/search")
    public ResponseEntity<?> searchKeywordInStats(
        @Parameter(description = "Access Token을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
        @Parameter(description = "검색할 명사를 입력해주세요.", required = true) @RequestParam String keyword
        ) {
        List<SearchKeywordInStatsRes> response = keywordService.searchKeywordInStats(userPrincipal, keyword);
        return ResponseEntity.ok(response);
    }




}
