package com.movelog.domain.record.presentation;

import com.movelog.domain.record.dto.request.CreateRecordReq;
import com.movelog.domain.record.service.RecordService;
import com.movelog.global.config.security.token.UserPrincipal;
import com.movelog.global.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("api/v1/record")
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;
    @Operation(summary = "기록 추가 API", description = "기록을 추가하는 API입니다.")
    @PostMapping
    public ResponseEntity<ApiResponse> createRecord(
            @Parameter(description = "User의 토큰을 입력해주세요.", required = true) @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Schemas의 CreateRecordReq를 참고해주세요.", required = true) @RequestPart CreateRecordReq createRecordReq,
            @RequestPart(value = "img", required = false) MultipartFile img) {
        recordService.createRecord(userPrincipal.getId(), createRecordReq, img);

        ApiResponse result = ApiResponse.builder()
                .check(true)
                .information("기록을 추가했어요")
                .build();
        return ResponseEntity.ok(result);
    }
    @Operation(summary = "오늘 기준 기록 현황 API", description = "오늘 기준 기록 확인하는 API입니다.")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse> retrieveTodayRecord(
            @Parameter(description = "User의 토큰을 입력해주세요.", required = false) @AuthenticationPrincipal UserPrincipal userPrincipal
            ) {;

        ApiResponse result = ApiResponse.builder()
                .check(true)
                .information(recordService.retrieveTodayRecord(5L))
                .build();
        return ResponseEntity.ok(result);
    }
}
