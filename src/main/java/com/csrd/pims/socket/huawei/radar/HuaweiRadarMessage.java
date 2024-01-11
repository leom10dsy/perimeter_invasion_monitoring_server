package com.csrd.pims.socket.huawei.radar;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class HuaweiRadarMessage {
    private byte[] header = new byte[2];
    private byte[] deviceId = new byte[2];
    // 发送命令类型，见《铁路安防雷达交互协议》
    private short functionCode = 0;
    private byte[] dataLength = new byte[2];
    private byte[] data;
    private byte[] dataCheck = new byte[2];

}
