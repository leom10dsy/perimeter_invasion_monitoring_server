package com.csrd.pims.bean.huawei.result;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

import java.util.List;

@Data
@XStreamAlias("Content")
public class HuaweiIvsAlarmLinkageEventData {

    private String id;
    @XStreamAlias("DomainCode")
    private String domainCode;
    @XStreamAlias("Action")
    private Action action;

    // 是否保存文件 0: 否 1: 是
    private int hasSaveFile;

    @Data
    public static class Action {
        @XStreamAlias("SrcDomainCode")
        private String srcDomainCode;
        @XStreamAlias("AlarmEventID")
        private String alarmEventID;
        @XStreamAlias("NotificationID")
        private String notificationID;
        @XStreamAlias("AlarmInCode")
        private String alarmInCode;
        @XStreamAlias("AlarmType")
        private String alarmType;
        @XStreamAlias("ActionType")
        private String actionType;
        @XStreamAlias("GlobalParam")
        private String globalParam;
        @XStreamAlias("isMailSnap")
        private String isMailSnap;
        @XStreamAlias("DevList")
        private List<DevInfo> devList;
        @XStreamAlias("UserList")
        private List<UserInfo> UserList;

    }

    @Data
    @XStreamAlias("DevInfo")
    public static class DevInfo {
        @XStreamAlias("DevCode")
        private String DevCode;
        @XStreamAlias("DevDomainCode")
        private String DevDomainCode;
        @XStreamAlias("Param")
        private String Param;

    }

    @Data
    @XStreamAlias("UserInfo")
    public static class UserInfo {
        @XStreamAlias("UserID")
        private String UserID;
        @XStreamAlias("UserDomainCode")
        private String UserDomainCode;
    }

}
