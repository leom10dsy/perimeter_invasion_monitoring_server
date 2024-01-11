package com.csrd.pims.enums;

import lombok.Getter;

@Getter
public enum AlarmStateEnum {
    START(1),
    MIDDLE(2),
    CLOSE(3);
    private final int value;

    AlarmStateEnum(int value) {
        this.value = value;
    }
}
