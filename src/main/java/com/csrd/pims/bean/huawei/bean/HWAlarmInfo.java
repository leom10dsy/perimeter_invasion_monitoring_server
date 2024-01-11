package com.csrd.pims.bean.huawei.bean;

import com.csrd.pims.enums.AlarmStateEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 华为报警信息记录
 */
@Getter
@Setter
@ToString
public class HWAlarmInfo {

    // 事件类型 ivs、nce
    private String alarmType;
    private int alarmLevel = 0;
    // 报警接收时间
    private Date alarmTime;
    // 报警事件id
    private String eventId;
    /**
     * 报警状态0报警结束、1报警开始、2报警进行中
     * 参考{@link AlarmStateEnum}
     */
    private int alarmState;
}
