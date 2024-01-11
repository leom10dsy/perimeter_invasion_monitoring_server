package com.csrd.pims.bean.huawei.param;

import lombok.Data;

@Data
public class HuaweiIvsRequest {
    private HuaweiIvsAlarmInfo alarmInfo;

    @Data
    public static class HuaweiIvsAlarmInfo {
        private Integer alarmEventId;
        private Integer notificationId;
    }
}
