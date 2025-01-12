package com.movelog.domain.news.presentation;

import com.movelog.domain.news.application.NewsService;
import com.movelog.domain.news.dto.request.NewsHeadLineReq;
import com.movelog.domain.news.dto.response.HeadLineRes;
import com.movelog.global.config.security.token.CurrentUser;
import com.movelog.global.config.security.token.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
            @ApiResponse(responseCode = "200", description = "뉴스 헤드라인 생성 성공"),
            @ApiResponse(responseCode = "400", description = "뉴스 헤드라인 생성 실패")
    })
    @PostMapping("/headline")
    public List<HeadLineRes> createHeadLine(
            @Parameter(description = "Access Token을 입력해주세요.", required = true) @CurrentUser UserPrincipal userPrincipal,
            @Parameter(description = "뉴스 헤드라인 생성 요청", required = true) @RequestBody NewsHeadLineReq newsHeadLineReq
    ) {
        return newsService.createHeadLine(userPrincipal, newsHeadLineReq);
    }



}
