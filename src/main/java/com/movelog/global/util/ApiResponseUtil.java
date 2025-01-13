package com.movelog.global.util;

import com.movelog.global.payload.ApiResponse;
import com.movelog.global.payload.ErrorResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseEntity;

import java.util.List;
@UtilityClass
public class ApiResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        ApiResponse<T> apiResponse = ApiResponse.<T>builder()
                .check(true)
                .information(data)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    public static <T> ResponseEntity<ApiResponse<List<T>>> success(List<T> data) {
        ApiResponse<List<T>> apiResponse = ApiResponse.<List<T>>builder()
                .check(true)
                .information(data)
                .build();

        return ResponseEntity.ok(apiResponse);
    }


}