package com.csrd.pims.bean.alarm;

import lombok.Data;

import java.util.List;

/**
 * 码流地址
 */
@Data
public class RPCStreamUrl {
    private List<ChannelInfo> channelInfo;
    private int errorCode;
    private String router;
    private String time;
}
