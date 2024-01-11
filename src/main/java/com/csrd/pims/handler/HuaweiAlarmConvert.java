package com.csrd.pims.handler;

import lombok.Getter;

@Getter
public enum HuaweiAlarmConvert {
    LEVEL_ONE(1 , 4),
    LEVEL_TWO(1 , 3),
    LEVEL_THREE(2 , 2),

    LEVEL_FOUR(3 , 1);

    private final int level;

    private final int huaweiLevel;

    HuaweiAlarmConvert(int level, int huaweiLevel) {
        this.level = level;
        this.huaweiLevel = huaweiLevel;
    }

    public static int alarmConvert(int huaweiLevel) {
        for (HuaweiAlarmConvert alarmConvert : HuaweiAlarmConvert.values()) {
            if (alarmConvert.getHuaweiLevel() == huaweiLevel) {
                return alarmConvert.getLevel();
            }
        }
        return LEVEL_ONE.level;
    }
}
