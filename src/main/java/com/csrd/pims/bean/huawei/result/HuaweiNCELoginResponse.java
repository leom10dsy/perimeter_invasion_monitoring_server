package com.csrd.pims.bean.huawei.result;

import lombok.Data;

/**
 * 登录返回数据
 */
@Data
public class HuaweiNCELoginResponse {
    //北向登录成功后，访问应用的接口时，将session设置到Http的header中，key为X-Auth-Token。
    //示例：X-Auth-Token:x-yyyyyyy。
    //说明
    //	从NCE-Super V100R018C10版本，Super域与NCE其他域保持一致，访问应用的接口时，将accessSession设置到
    //请求Headers参数的X-AUTH-TOKEN中。例如：
    //X-AUTH-TOKEN:x-vyg4unlhs95efzk7obikleap0a4489teul48imikqkry48bt
    //	对于NCE-Super V100R018C00及之前已对接过的版本，兼容支持将accessSession的值设置到Cookie中的方式
    private String accessSession;
    //访问非GET的ROA接口时，需要在请求头中添加返回的roaRand，key为roarand。
    private String roaRand;
    //会话在该时间内没有响应时失效，单位：秒。0表示永远不失效。
    private String expires;
    //登录扩展信息。“expires”对应值表示密码有效期剩余时间，单位为天；“passwdstatus”对应值表示密码状态，若为“expiring”表示密码即将过期状态。
    private String additionalInfo;
}
