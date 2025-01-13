package com.movelog.domain.record.exception;


import com.movelog.global.exception.NotFoundException;

public class KeywordNotFoundException extends NotFoundException {

    public KeywordNotFoundException() {
        super("U002", "키워드를 찾을 수 없습니다.");
    }
}
