package com.movelog.global.error;


import com.example.ai_tutor.global.payload.ErrorCode;
import lombok.Getter;

@Getter
public class DefaultException extends RuntimeException{

    private ErrorCode errorCode;

    public DefaultException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public DefaultException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
