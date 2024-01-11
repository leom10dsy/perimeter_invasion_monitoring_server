package com.csrd.pims.bean.huawei.param;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * 华为振动光纤报警通知数据
 */
@Data
public class HuaweiNCEAlarmInfo {

    private List<Notifications> notifications;

    @Data
    public static class Notifications {
        private NotificationsData data;
    }

    @Data
    public static class NotificationsData {
        //上报事件的UUID "02d11f40-4717-11e9-9872-286ed488d730"
        private String eventUUID;
        //	Critical-紧急;
        //	Major-重要;
        //	Minor-次要;
        //	Warning-提示;
        private String alarmLevel;
        //该事件所属的光纤的UUID "bfd7a0df-9a33-11e9-8d82-0050568bab29"
        private String fiberUUID;
        //上报该告警的设备UUID "42d11f40-4717-51e9-9872-g86ed488d380"
        private String neUUID;
        //上报该告警的槽位ID
        private Integer slotId;
        //上报该告警的端口ID
        private Integer portId;

        //事件首次上报的时间
        private Long firstReportTime;

        //事件最后一次上报的时间
        private Long latestReportTime;

        @SerializedName(value = "endTime", alternate = {"endtime"})
        //事件结束的时间
        private Long endTime;
        //经度
        private Float longitude;
        //纬度
        private Float latitude;
        @SerializedName(value = "start-pile", alternate={"start_pile"})
        //事件发生的起始光桩的UUID
        private String start_pile;
        @SerializedName(value = "end-pile", alternate={"end_pile"})
        //事件发生的终止光桩的UUID
        private String end_pile;
        //事件发生的距离起始光桩的距离,单位为m。
        private Float offset;
        //预测的入侵类型，当前的取值范围是：[0, 2]。
        //	0：断纤
        //	1：误报/干扰
        //	2：入侵
        private Integer forecastIntrusionType;

        //事件发生位置距设备的光缆距离
        private Float riskPointDistance;
    }

}
