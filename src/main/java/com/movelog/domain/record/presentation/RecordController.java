package com.movelog.domain.record.presentation;

import com.movelog.domain.record.dto.request.CreateRecordReq;
import com.movelog.domain.record.dto.response.RecentRecordImagesRes;
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
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        recordService.createRecord(5L, createRecordReq, img);
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


}
