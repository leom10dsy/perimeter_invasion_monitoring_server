package com.csrd.pims.service;

import com.csrd.pims.amqp.tk.TKAlarmInfo;

import java.util.Date;

public interface HuaweiIvsMediaService {

    /**
     * 实时图片抓拍
     *
     * @param cameraNumber 摄像设备code
     * @return 图片访问路径
     */
    String capturePic(String cameraNumber);

    /**
     * 下载视频
     *
     * @param cameraNumber 相机ip
     * @param alarmGuid    报警ip
     * @param alarmTime    报警时间
     */
    void downloadIvsVideo(String cameraNumber, String alarmGuid, String alarmVideoPath, Date alarmTime);

    /**
     * 添加下载视频到下载队列
     *
     * @param cameraNumber 相机ip
     * @param alarmGuid    报警ip
     * @param alarmTime    报警时间
     */
    void addDownloadAlarmIvsVideoQueue(String cameraNumber, String alarmEventId, String alarmVideoPath, Date alarmTime);

    String uploadImg(TKAlarmInfo alarmInfo, String LocalFileName);
}
