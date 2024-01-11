package com.csrd.pims.socket.huawei.radar;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 发消息给控制板信息
 */
@Data
@Accessors(chain = true)
public class HuaweiRadarRequest {

    private byte[] header = new byte[]{(byte) 0xFA, (byte) 0xFB, (byte) 0x00, (byte) 0x00};
    private byte[] id = new byte[]{(byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD};
    private byte[] sendUser = new byte[]{(byte) 0x00};
    private byte[] handle = new byte[]{(byte) 0x01};
    private byte[] commandType;
    /* 传输类型 */
    private byte[] typeData;
    /* 控制状态 */
    private byte[] dataCheck;

    public int getLength() {
        int idL = id != null ? id.length : 0;
        int sendUserL = sendUser != null ? sendUser.length : 0;
        int handleL = handle != null ? handle.length : 0;
        int commandTypeL = commandType != null ? commandType.length : 0;
        int typeDataL = typeData != null ? typeData.length : 0;
        int dataCheckL = dataCheck != null ? dataCheck.length : 0;
        return idL + sendUserL + handleL + commandTypeL + typeDataL + dataCheckL;
    }
}
