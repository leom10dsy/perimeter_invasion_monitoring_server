package com.csrd.pims.bean.huawei.radar;

import lombok.Data;

/**
 * 华为毫米波雷达数据
 */
@Data
public class HwMMWTargetData {

    private int targetId;
    private byte targetType;
    // 是否发送摄像头 输出报警0（不发摄像头），输出报警1（发送给摄像头）
    private byte sendCamera;
    // F7 FF 横坐标
    private float targetX;
    // 31 01 纵坐标
    private float targetY;
    // 00 00 x速度
    private float targetXSpeed;
    // FF FF y速度
    private float targetYSpeed;
    // 00 00 信噪比
    private int SNR;
    // 0A 00 分数
    private int score;
    // 01 00 00 00 保留字
    private int reserved;
    // 创建时间
    private long createTime;


    public HwMMWTargetData(int targetId, byte type, byte sendCamera, float x, float y, float xSpeed, float ySpeed, int SNR, int score, int reserved) {
        this.targetId = targetId;
        this.targetType = type;
        this.sendCamera = sendCamera;
        this.targetX = x;
        this.targetY = y;
        this.targetXSpeed = xSpeed;
        this.targetYSpeed = ySpeed;
        this.SNR = SNR;
        this.score = score;
        this.reserved = reserved;
    }

    public HwMMWTargetData() {
    }
}
