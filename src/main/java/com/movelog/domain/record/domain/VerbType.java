package com.movelog.domain.record.domain;

import lombok.Getter;

@Getter
public enum VerbType {
    // 했어요, 먹었어요, 갔어요
    DO("했어요"), EAT("먹었어요"), GO("갔어요");

    private final String verbType;

    VerbType(String verbType) {
        this.verbType = verbType;
    }

    // Enum 매핑 메서드 추가
    public static VerbType fromValue(String value) {
        for (VerbType type : values()) {
            if (type.getVerbType().equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }

    public static String getStringVerbType(VerbType verbType) {
        return verbType.getVerbType();
    }

}
