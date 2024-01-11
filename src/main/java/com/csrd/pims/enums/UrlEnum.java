package com.csrd.pims.enums;

import lombok.Getter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

/**
 * 保存各个对接平台请求url
 */
@Getter
public enum UrlEnum {

    // 华为NCE振动光纤接口
    HUAWEI_NCE_LOGIN("/rest/plat/smapp/v1/sessions", "put", "NCE", "华为振动光纤_登录"),
    HUAWEI_NCE_LOGOUT("/rest/plat/smapp/v1/sessions", "delete", "NCE", "华为振动光纤_登出"),
    HUAWEI_NCE_SUBSCRIPTION("/restconf/v1/operations/huawei-nce-notification-action:establish-subscription",
            "post", "NCE", "华为振动光纤_订阅"),
    HUAWEI_NCE_DELETE_SUBSCRIPTION("/restconf/v1/operations/huawei-nce-notification-action:delete-subscription",
            "post", "NCE", "华为振动光纤_去订阅"),
    HUAWEI_NCE_SSE("/restconf/streams/sse/v1/identifier",
            "post", "NCE", "华为振动光纤_创建长连接"),
    HUAWEI_NCE_FIBER("/rest/v1/distributed-acoustic-sensor/fiber",
            "get", "NCE", "华为振动光纤_查询光纤信息"),
    HUAWEI_NCE_FIBER_PILE("/rest/v1/distributed-acoustic-sensor/fiberpile",
            "get", "NCE", "华为振动光纤_查询光桩信息"),

    // 华为ivs设备接口
    HUAWEI_IVS_LOGIN("/loginInfo/login/v1.0", "post", "IVS", "华为ivs_登录"),
    HUAWEI_IVS_KEEP_LIVE("/common/keepAlive", "get", "IVS", "华为ivs_保活"),
    HUAWEI_IVS_LOGOUT("/users/logout", "get", "IVS", "华为ivs_登出"),
    HUAWEI_IVS_DEVICE_LIST("/device/deviceList/v1.0?deviceType=2&fromIndex=1&toIndex=1000", "get", "IVS", "华为ivs_设备列表"),


    HUAWEI_IVS_PTZPRESET_LIST("/device/ptzpresetlist", "get", "IVS", "华为IVS_查询预置位列表"),


    HUAWEI_IVS_DEVICE_PTZ_CONTROL("/device/ptzcontrol", "post", "IVS", "华为ivs_切换预制点"),
    HUAWEI_IVS_REGISTER_CALLBACK("/users/regeditcallback", "post", "IVS", "华为ivs_注册回调"),
    HUAWEI_IVS_ALARM_SNAPSHOT_LIST("/snapshot/alarmsnapshotlist/v1.0", "post", "IVS", "华为ivs_获取报警图片"),
    HUAWEI_IVS_ALARM_CONFIRM("alarm/confirm/v1.0", "post", "IVS", "华为ivs_报警确认"),
    HUAWEI_IVS_SUBSCRIBE_ALARM("device/subscribealarm", "post", "IVS", "华为ivs_告警订阅"),

    HUAWEI_IVS_INTELLIGENT_SUBSCRIBE_ALARM_ADD("/sdk_service/rest/subscribes", "post", "IVS", "华为ivs_智能告警订阅添加"),

    HUAWEI_IVS_INTELLIGENT_SUBSCRIBE_ALARM_DELETE("/sdk_service/rest/subscribes", "delete", "IVS", "华为ivs_智能告警订阅删除"),

    HUAWEI_ITS_RTSPURL("/video/rtspurl/v1.0", "post", "ITS", "华为its_获取实时视频url"),
    ;

    UrlEnum(String uri, String requestType, String platform, String platformName) {
        this.uri = uri;
        this.requestType = requestType;
        this.platform = platform;
        this.platformName = platformName;
    }

    private final String uri;
    private final String requestType;
    private final String platform;
    private final String platformName;

    /**
     * 获取请求地址
     */
    public static String getRequestUrl(UrlEnum urlEnum, boolean isSslRequest, String ip, String port) {
        String protocol = isSslRequest ? "https://" : "http://";
        String uri = urlEnum.uri.indexOf("/") == 0 ? urlEnum.uri.substring(1) : urlEnum.uri;
        return protocol + getRequestAddress(urlEnum, ip, port) + "/" + uri;

    }

    /**
     * 根据平台获取ip端口号
     */
    public static String getRequestAddress(UrlEnum urlEnum, String ip, String port) {
        String address = ip + ":" + port;

//        switch (urlEnum.platform.toUpperCase()) {
//            case "NCE":
//                address = Params.NCE_IP + ":" + Params.NCE_PORT;
//                break;
//            case "IVS":
//                address = Params.IVS_IP + ":" + Params.IVS_PORT;
//                break;
//        }
        return address;
    }

    /**
     * 添加rest请求参数
     *
     * @param urlEnum      url枚举
     * @param pathParam    参数
     * @param isSslRequest 是否https
     * @return url
     */
    public static String getRestfulUri(UrlEnum urlEnum, Set<String> pathParam, boolean isSslRequest, String ip, String port) {
        StringBuilder urlBuilder = new StringBuilder(getRequestUrl(urlEnum, isSslRequest, ip, port));
        pathParam.forEach(value -> {
            urlBuilder.append("/{").append(value).append("}");
        });
        return urlBuilder.toString();
    }

    /**
     * 添加请求参数
     *
     * @param urlEnum      url枚举
     * @param pathParam    参数
     * @param isSslRequest 是否https
     * @return url
     */
    public static String addUrlParam(UrlEnum urlEnum, Map<String, String> pathParam, boolean isSslRequest, String ip, String port) {
        return addUrlParam(getRequestUrl(urlEnum, isSslRequest, ip, port), pathParam);
    }

    /**
     * 添加请求参数
     *
     * @param pathParam 参数
     * @return url
     */
    public static String addUrlParam(String url, Map<String, String> pathParam) {
        StringBuilder urlBuilder = new StringBuilder(url);
        urlBuilder.append("?");
        pathParam.forEach((key, value) -> {
            urlBuilder.append(key).append("=").append(value).append("&");
        });
        return urlBuilder.substring(0, urlBuilder.lastIndexOf("&"));
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String encode = URLEncoder.encode("huawei-nce-notification-action:establish-subscription", "utf-8");
        System.out.println(encode);
    }
}
