package com.csrd.pims.rabbitMq.domain;

import com.csrd.pims.bean.huawei.radar.HwMMWTargetData;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter

public class HuaweiAlarm {

    private String alarmuuid;

    private String stationId;

    private String ivsNumber;

    private String ivsName;

    private String cameraNumber;

    private String radarid;

    private Integer level;

    private Date alarmTime;

    private String cameraName;

    private List<HwMMWTargetData>  hwMMWTargetDataList;

    private String isHistory;

    private String isAlarmed;

    private int alarmSource;

    private String alarmSourceName;

    private String historyStream;

    private String realtimeStream;

    /**
     * 如果是单nce报警 ： nce名称
     *  摄像头名称
     */
    private Boolean isNceSingle;

    private String nceIp;

    private String distance;

}
