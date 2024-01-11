package com.csrd.pims.bean.web;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ivs报警确认
 */
@Data
@Accessors(chain = true)
public class HuaweiIvsConfirm {
    // 告警事件ID。特别说明：当告警是通过5.10.10智能告警数据推送接收时，该字段填-1，并在notificationId字段填上对应的NotificationID。
    private Integer alarmEventId;

    // 告警源编码（当前保留参数，未启用，不作校验）
    private String alarmInCode;
    // 告警处理信息 AlarmOperateInfo
    private OperateInfo operateInfo;

    //智能告警订阅
    private String notificationId;

    @Data
    @Accessors(chain = true)
    public static class OperateInfo {
        // 处理人ID，即处理人用户 ID
        private Integer operatorId;
        // 处理人名，字符串。汉字、英文字母、数字、中划线和下划线，长度不超过128
        private String operatorName;
        // 告警处理时间
        private Long operateTime;
        // 保留字段
        private String reserver;
        // 告警处理人员输入的描述信息，内容可以为空。键盘可见字符，长度不超过64
        private String operateInfo;
    }
}
