package com.movelog.domain.user.exception;


import com.movelog.global.exception.DuplicateException;

public class UserDuplicateException extends DuplicateException {

    public UserDuplicateException() {
        super("U002", "이미 존재하는 사용자입니다.");
    }
}
