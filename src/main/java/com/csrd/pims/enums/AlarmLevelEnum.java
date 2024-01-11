package com.csrd.pims.enums;

/**
 * @description: 报警级别
 * @author: shiwei
 * @create: 2022-06-22 11:18:58
 **/
public enum AlarmLevelEnum {

    LEVEL_ONE(1),
    LEVEL_TWO(2),
    LEVEL_THREE(3);

    private final int level;

    AlarmLevelEnum(int level) {
        this.level = level;
    }

    /**
     * @descripion: get the integer level value of this alarm
     */
    public int getLevel() {
        return level;
    }

}
