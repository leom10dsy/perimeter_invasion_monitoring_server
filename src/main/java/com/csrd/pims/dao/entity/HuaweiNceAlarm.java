package com.csrd.pims.dao.entity;

import lombok.Data;

import java.util.Date;

@Data
public class HuaweiNceAlarm {

    private Date alarmTime;

    private Float distance;

    /**
     * 预置位索引号
     */
    private String presetIndex;


    private String cameraName;

    private Boolean presetResult;


}
