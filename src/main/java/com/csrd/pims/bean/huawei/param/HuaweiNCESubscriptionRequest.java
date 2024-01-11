package com.csrd.pims.bean.huawei.param;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 订阅报警请求参数
 */
@Data
@Accessors(chain = true)
public class HuaweiNCESubscriptionRequest {

    //{"input":{"encoding":"encode-json","protocol":"sse","subscription":[
    // {"topic":"das-resources","incident-conditions":
    // {
    // "priorities":["critical","high"],"impacts":[],"urgencies":[],"incidentNames":["EVENT","FIBERPILE"]
    // }}
    // ]}}

    //编码格式，默认为encode-json
    private String encoding;
    //协议类型，当前取值支持sse
    private String protocol;
    //incidentNames中参数为需要上报的数据类型，比如EVENT为事件，FIBERPILE为光桩，可根据需求填写
    private List<HuaweiNCESubscriptionInfo> subscription;

    @Data
    public static class HuaweiNCESubscriptionInfo {
        private String topic;
        @SerializedName("incident-conditions")
        private HuaweiNCEIncidentConditions incident_conditions;
    }

    @Data
    public static class HuaweiNCEIncidentConditions {
        private List<String> priorities;
        private List<String> impacts;
        private List<String> urgencies;
        @SerializedName("incident-names")
        private List<String> incidentNames;
    }

}
