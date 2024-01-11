package com.csrd.pims.bean.huawei.result;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
@XStreamAlias("Content")
public class HuaweiIvsAlarmEventData {

    private String id;
    @XStreamAlias("AlarmEventId")
    private String alarmEventId;
    @XStreamAlias("AlarmInType")
    private String alarmInType;
    @XStreamAlias("AlarmDesc")
    private String alarmDesc;
    @XStreamAlias("AlarmType")
    private String alarmType;
    @XStreamAlias("AlarmTypeName")
    private String alarmTypeName;
    @XStreamAlias("OccurTime")
    private String occurTime;
    @XStreamAlias("AlarmInCode")
    private String alarmInCode;
    @XStreamAlias("OccurNumber")
    private String occurNumber;
    @XStreamAlias("AlarmInName")
    private String alarmInName;
    @XStreamAlias("isUserDefind")
    private String isUserDefined;
    @XStreamAlias("DevDomainCode")
    private String devDomainCode;
    @XStreamAlias("IsCommission")
    private String isCommission;
    @XStreamAlias("FileId")
    private String fileId;
    @XStreamAlias("FileIdEx")
    private String fileIdEx;
    @XStreamAlias("PreviewUrl")
    private String previewUrl;
    @XStreamAlias("AlarmLevelValue")
    private String alarmLevelValue;
    @XStreamAlias("AlarmLevelName")
    private String alarmLevelName;
    @XStreamAlias("AlarmLevelColor")
    private String alarmLevelColor;
    @XStreamAlias("AlarmStatus")
    private String alarmStatus;
    @XStreamAlias("AlarmCategory")
    private String alarmCategory;
    @XStreamAlias("NVRCode")
    private String NVRCode;
    @XStreamAlias("ExistsRecord")
    private String existsRecord;
    @XStreamAlias("ExtParam")
    private String extParam;
    // 是否保存文件 0: 否 1: 是
    private int hasSaveFile;

}
