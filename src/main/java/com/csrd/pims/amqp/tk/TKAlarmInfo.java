package com.csrd.pims.amqp.tk;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@TableName("TKAlarmInfo")
@Accessors(chain = true)
public class TKAlarmInfo {

    @TableId("companyAlarmId")
    private String companyAlarmId;

    @TableField("alarmEventId")
    private String alarmEventId;

    @TableField("companyCode")
    private String companyCode;

    @TableField("algorithmCode")
    private String algorithmCode;

    @TableField("areaCode")
    private String areaCode;

    @TableField("position")
    private String position;

    @TableField("companyAlarmDate")
    private String companyAlarmDate;

    @TableField("alarmVideo")
    private String alarmVideo;

    @TableField("alarmImage")
    private String alarmImage;

    @TableField("alarmSrcFile")
    private String alarmSrcFile;

    /* ---2023年11月,铁科新需求增加---*/
    @TableField("lineInfo")
    private String lineInfo;

    @TableField("cameraChannelId")
    private String cameraChannelId;

    @TableField("cameraDeviceId")
    private String cameraDeviceId;

    @TableField("deviceId")
    private String deviceId;

    @TableField("alarmState")
    private Integer alarmState;

    @TableField("alarmLevel")
    private Integer alarmLevel;

    @TableField("alarmType")
    private String alarmType;

    @TableField("isPushAlarm")
    private Integer isPushAlarm;
}
