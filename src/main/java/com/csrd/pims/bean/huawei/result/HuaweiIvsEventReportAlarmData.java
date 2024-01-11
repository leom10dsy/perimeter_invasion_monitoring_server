package com.csrd.pims.bean.huawei.result;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

@Data
@XStreamAlias("content")
public class HuaweiIvsEventReportAlarmData {


    private String id;
    @XStreamAlias("AlarmEventId")
    private String alarmEventId;
    @XStreamAlias("AlarmInCode")
    private String alarmInCode;
    @XStreamAlias("DevDomainCode")
    private String devDomainCode;
    @XStreamAlias("AlarmInType")
    private String alarmInType;
    @XStreamAlias("AlarmInName")
    private String alarmInName;
    @XStreamAlias("AlarmLevelValue")
    private String alarmLevelValue;
    @XStreamAlias("AlarmLevelName")
    private String alarmLevelName;
    @XStreamAlias("AlarmLevelColor")
    private String alarmLevelColor;
    @XStreamAlias("isUserDefind")
    private String isUserDefind;
    @XStreamAlias("IsUserDefined")
    private String isUserDefined;
    @XStreamAlias("AlarmType")
    private String alarmType;
    @XStreamAlias("AlarmTypeName")
    private String alarmTypeName;
    @XStreamAlias("ThirdAlarmType")
    private String thirdAlarmType;
    @XStreamAlias("AlarmCategory")
    private String alarmCategory;
    @XStreamAlias("OccurTime")
    private String occurTime;
    @XStreamAlias("OccurNumber")
    private String occurNumber;
    @XStreamAlias("AlarmStatus")
    private String alarmStatus;
    @XStreamAlias("IsCommission")
    private String isCommission;
    @XStreamAlias("FileId")
    private String fileId;
    @XStreamAlias("FileIdEx")
    private String fileIdEx;
    @XStreamAlias("PreviewUrl")
    private String previewUrl;
    @XStreamAlias("ExistsRecord")
    private String existsRecord;
    @XStreamAlias("NVRCode")
    private String nVRCode;
    @XStreamAlias("AlarmDesc")
    private String alarmDesc;
    @XStreamAlias("ExtParam")
    private String extParam;
    @XStreamAlias("LocationInfo")
    private String locationInfo;
    @XStreamAlias("NetElementID")
    private String netElementID;
    @XStreamAlias("NetName")
    private String netName;
    @XStreamAlias("AlarmCleanType")
    private String alarmCleanType;
    @XStreamAlias("Reserve")
    private String reserve;

    @XStreamImplicit
    private List<AlarmLinkageInfo> alarmLinkageInfoList;

    // 是否保存文件 0: 否 1: 是
    private int hasSaveFile;

    @Data
    @XStreamAlias("AlarmLinkageInfoList")
    public static class AlarmLinkageInfo {
        @XStreamAlias("linkageId")
        private String linkageId;
        @XStreamAlias("actionList")
        private List<AlarmInfo> actionList;
    }

    @XStreamAlias("action")
    @Data
    public static class AlarmInfo {
        @XStreamAlias("actionType")
        private String actionType;
        @XStreamAlias("globalParam")
        private String globalParam;
        @XStreamAlias("actionBranch")
        private String actionBranch;
        @XStreamAlias("devList")
        private List<DevInfo> devList;

    }

    @Data
    @XStreamAlias("devInfo")
    public static class DevInfo {
        @XStreamAlias("devCode")
        private String devCode;
        @XStreamAlias("param")
        private String param;

    }

}
