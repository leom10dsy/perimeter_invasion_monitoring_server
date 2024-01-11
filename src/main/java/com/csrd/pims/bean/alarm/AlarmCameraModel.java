package com.csrd.pims.bean.alarm;

import java.util.Date;

public class AlarmCameraModel {
    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    public int getPresetPoint() {
        return presetPoint;
    }

    public void setPresetPoint(int presetPoint) {
        this.presetPoint = presetPoint;
    }



    public Date getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    public int getLinkType() {
        return linkType;
    }

    public void setLinkType(int linkType) {
        this.linkType = linkType;
    }

    public String getRadarId() {
        return radarId;
    }

    public void setRadarId(String radarId) {
        this.radarId = radarId;
    }

    public int getGunChanelId() {
        return gunChanelId;
    }

    public void setGunChanelId(int gunChanelId) {
        this.gunChanelId = gunChanelId;
    }
    public int getBallChanelId() {
        return ballChanelId;
    }
    public void setBallChanelId(int ballChanelId) {
        this.ballChanelId = ballChanelId;
    }
    //报警ID
    private String alarmId;
    //预置点
    private int presetPoint;
    //报警时间
    private Date alarmTime;
    //球机通道ID
    private int ballChanelId;
    //枪机通道(枪机通道应用于枪球联动模式时生效)
    private int gunChanelId;
    //联动方式 0为预置点模式 1为枪球联动
    private int linkType;
    //雷达ID
    private String radarId;

    public boolean getIsPtz() {
        return isPtz;
    }

    public void setPtz(boolean ptz) {
        isPtz = ptz;
    }

    private boolean isPtz;

    public String getCameraIp() {
        return cameraIp;
    }

    public void setCameraIp(String cameraIp) {
        this.cameraIp = cameraIp;
    }
    //摄像头IP 针对华为摄像头根据IP获取对应的code
    private String cameraIp;
}
