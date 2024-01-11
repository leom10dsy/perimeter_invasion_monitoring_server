package com.csrd.pims.service;

import com.csrd.pims.bean.huawei.param.PTZControlModel;
import com.csrd.pims.bean.huawei.result.HuaweiPTZPresetInfo;
import com.csrd.pims.dao.entity.ivs.HuaweiCamera;

import java.util.Date;
import java.util.List;

/**
 * 华为雷视融合服务
 */
public interface HuaweiIvsService {

    /**
     * 登录ivs
     *
     * @param ip       ivsIp
     * @param port     ivsPort
     * @param userName 用户名
     * @param password 密码
     * @return 登录结果
     */
    boolean login(String ip, String port, String userName, String password);

    /**
     * 保活
     *
     * @param ivsIp   ivsIp
     * @param ivsPort ivsPort
     * @param cookie  cookie
     * @return 保活结果
     */
    boolean keepLive(String ivsIp, String ivsPort, String cookie);

    /**
     * ivs登出
     *
     * @param ip     ivsIP
     * @param port   ivsPort
     * @param cookie cookie
     * @return 登出结果
     */
    boolean logout(String ip, String port, String cookie);


    /**
     * 或者预置位列表
     *
     * @param cameraNumber 摄像头编号
     * @param ivsNumber    ivs编号
     * @param ip           ivsIP
     * @param port         ivsPort
     * @param cookie       cookie
     * @return 预置位列表
     */
    List<HuaweiPTZPresetInfo> getHuaweiPTZPresetInfoList(String cameraNumber, String ivsNumber, String ip, String port, String cookie);


    /**
     * 转动预置位
     *
     * @param ptzControlModel 预置位信息
     * @param port            ivsPort
     * @param cookie          cookie
     * @return 转动结果
     */
    boolean hwPtzPoint(PTZControlModel ptzControlModel, String ip, String port, String cookie);


    /**
     * 获取视频下载流
     *
     * @param cameraNumber 摄像头编号
     * @param alarmTime    报警时间
     * @param type         类型
     * @return 视频流
     */
    String getVideoRtspUrl(String cameraNumber, Date alarmTime, int type);


    /**
     * 直接拼接视频流
     *
     * @param ip           ivsIp
     * @param port         ivsPort
     * @param cameraNumber 摄像头编号
     * @param type         实时 还是录像流
     * @param alarmTime    报警时间
     * @return 拼接流字符串
     */
    String getStream(String ip, String port, String cameraNumber, int type, Date alarmTime);

    /**
     * 添加智能订阅
     * @param ip ivsIp
     * @param port ivsPort
     * @param cookie cookie
     * @param cameraNumber 摄像头编号
     * @return 订阅ID
     */
    String addSubscribeIntelligentAlarm(String ip, String port, String cookie, String cameraNumber);


    /**
     * 删除智能订阅
     *
     * @param ip          ivsIp
     * @param port        IVSPort
     * @param cookie      cookie
     * @param SubscribeID 订阅ID
     * @return 删除结果
     */
    Boolean deleteSubscribeIntelligentAlarm(String ip, String port, String cookie, String SubscribeID);


    /**
     * 智能订阅处理报警
     *
     * @param callbackVo
     */
    void huaweiIvsIntelligentAlarmHandle(String callbackVo);

    List<HuaweiCamera> getAllCameraByIpAndPort();


    /**
     * 根据摄像头编号获得实时流
     *
     * @param cameraNumber 摄像头编号
     * @return 实时流地址
     */
    String getRealRtspurl(String cameraNumber);

    /**
     * 超时未接收到报警，球机抓拍图片，推送结束报警，并入库
     * @param eventPrefix
     * @param currentDate
     */
    void pushCloseAlarm(String eventPrefix,Date currentDate);
}
