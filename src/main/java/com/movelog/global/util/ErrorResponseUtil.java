package com.movelog.global.util;

import com.movelog.global.payload.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ErrorResponseUtil {

    public static ResponseEntity<ErrorResponse> failure(
            String errorCode, HttpStatus status, String message, Class<?> clazz) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode)
                .status(status.value())
                .message(message)
                .clazz(clazz.getSimpleName())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

}

