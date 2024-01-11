package com.csrd.pims.bean.huawei.param;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 订阅报警请求参数
 */
@Data
@Accessors(chain = true)
public class HuaweiNCESubscriptionResponse {

    //订阅结果取值范围为：success、fail。
    @SerializedName("subscription-result")
    private String subscriptionResult;
    //用户标识
    private String identifier;
    //创建通知通道的URL
    private String url;

}
