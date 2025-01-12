package com.movelog.global.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final int statusCode;
    private final String code;
    private final String message;

    public NotFoundException(String code, String message) {
        super(message);
        this.statusCode = 404;
        this.code = code;
        this.message = message;
    }
}
