package com.csrd.pims.enums;

import lombok.Getter;

@Getter
public enum AlarmEnum {
    WAIT_CONFIRM(0, "待确认"),
    ALARM_CONFIRM(1, "确认报警"),
    FALSE_ALARM(2, "确认误报"),
    DELAYED_CONFIRM(3, "延迟处理"),
    ALARM_RELIEVE(4, "报警解除"),
    TRAIN_PASSING_ALARM_CANCEL(5, "过车报警解除"),
    DEFAULT(999, "UNKNOWN"),
    ;
    private final int code;
    private final String msg;

    AlarmEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static AlarmEnum findByState(int state) {
        for (AlarmEnum value : values()) {
            if (value.getCode() == state) {
                return value;
            }
        }
        return DEFAULT;
    }
}
