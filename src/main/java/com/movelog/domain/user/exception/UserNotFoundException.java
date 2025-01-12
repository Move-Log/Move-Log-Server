package com.movelog.domain.user.exception;

import com.movelog.global.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException() {
        super("U001", "사용자를 찾을 수 없습니다.");
    }
}
