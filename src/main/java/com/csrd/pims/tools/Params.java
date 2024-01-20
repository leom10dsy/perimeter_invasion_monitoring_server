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

    // 本地媒体储存路径 D:\huawei\alarmfile\
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

}
