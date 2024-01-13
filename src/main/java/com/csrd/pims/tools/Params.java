package com.csrd.pims.tools;

import com.csrd.pims.bean.huawei.bean.HWAlarmInfo;
import com.csrd.pims.bean.huawei.param.HuaweiNCESubscriptionResponse;
import com.csrd.pims.dao.entity.ivs.HuaweiCamera;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @description: 全局参数
 * @author: shiwei
 * @create: 2022-06-21 16:57:57
 **/
public class Params {

    // 本地媒体储存路径
    public static String LOCAL_STORAGE_PATH;

    public static Boolean LOCAL_DELETE_FLAG;
    public static String LOCAL_IP;
    public static String ivsCookie = null;

    public static List<HuaweiCamera> huaweiCameras;

    public static String nceCookie = null;
    public static String nceRoaRand = null;
    public static Date nceKeepTime = new Date();

    public static Float nceDistance;
    public static HuaweiNCESubscriptionResponse nceSubscriptionResponse;

    public static volatile AtomicBoolean IVS_ALARM_ENABLE = new AtomicBoolean(true);
    public static volatile AtomicBoolean NCE_ALARM_ENABLE = new AtomicBoolean(true);

    // <alarmEventId, Date>
    public static ConcurrentHashMap<String, HWAlarmInfo> LATEST_ALARM_TIME = new ConcurrentHashMap<>();

    // 故障原因  0 无异常 1激光雷达故障 2毫米波雷达故障 3摄像头故障 4盒子故障 5 震动光纤多个使用,分割,初始化0
    public static ConcurrentHashMap<String, String> FAILURE_CAUSE = new ConcurrentHashMap<>();

}
