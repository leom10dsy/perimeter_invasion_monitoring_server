package com.csrd.pims.bean.web;

import lombok.Data;

import java.util.List;

@Data
public class HuaweiIvsDispositionNotificationCallback {
    public DispositionNotificationListObject DispositionNotificationListObject;


    @Data
    public static class DispositionNotificationListObject{
        private List<DispositionNotificationObject> DispositionNotificationObject;
    }

    @Data
    public static class DispositionNotificationObject{

        private int AlarmLevel;

        private String TriggerTime;

        private String SubscribeID;

        private BehaviorAnalysisObject BehaviorAnalysisObject;

        private AlarmLinkageInfo AlarmLinkageInfo;
    }

    @Data
    public static class BehaviorAnalysisObject{
        private SubImageList SubImageList;

        private Integer RadarTargetID;
    }

    @Data
    public static class AlarmLinkageInfo{
        private List<ActionLinkageModel> actionList;

        private Integer AlarmMatch;

        private Integer AlarmRuleType;

        private String AnalysisTaskID;
    }

    @Data
    public static class SubImageList{
        private List<SubImageInfoObject> SubImageInfoObject;
    }

    @Data
    public static class SubImageInfoObject{
        private String Data;
    }

    @Data
    public static class ActionLinkageModel{
        private Integer actionBranch;

        private Integer actionType;

        private Integer globalParam;

        private List<DevInfo> devList;
    }

    @Data
    public static class DevInfo{
        private String devCode;

        private String param;
    }
}
