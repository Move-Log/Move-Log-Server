package com.movelog.global.exception;

import lombok.Getter;

@Getter
public class DuplicateException extends RuntimeException {
    private final int statusCode;
    private final String code;
    private final String message;

    public DuplicateException(String code, String message) {
        super(message);
        this.statusCode = 409; // Conflict status code
        this.code = code;
        this.message = message;
    }
}
