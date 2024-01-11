package com.csrd.pims.bean.huawei.param;

import lombok.Data;

@Data
public class MediaURL {
    //是否支持组播 0：不支持 1支持
    private int broadCastType;
    //协议类型 1UDP 2TCP
    private int protocolType;
    //打包协议类型1:ES(默认)
    private int packProtocolType;
    //1实时预览 3录像下载 4录像回放
    private int serviceType;
    //码流类型 1主码流 2辅码流
    private int streamType;
    //是否直连优化(默认0) 0否 1是
    private int transMode;
    //客户端类型 0IVSCU 1标准RTSP
    private int clientType;
    private TimeSpan timeSpan;
}
