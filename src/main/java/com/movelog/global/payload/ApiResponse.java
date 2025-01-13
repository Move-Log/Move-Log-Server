package com.movelog.global.payload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
public class ApiResponse<T> {

    @Schema(type = "boolean", example = "true", description = "로직 처리 성공 여부를 반환합니다.")
    private final boolean check;

    @Schema(type = "object", description = "응답 데이터를 담는 필드입니다.")
    private final T information;

    @Builder
    public ApiResponse(boolean check, T information) {
        this.check = check;
        this.information = information;
    }

    // 정적 팩토리 메서드로 명확한 응답 생성
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    public static <T> ApiResponse<T> failure(T errorMessage) {
        return new ApiResponse<>(false, errorMessage);
    }
}
