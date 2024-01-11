package com.csrd.pims.bean.alarm;

import lombok.Data;

import java.util.List;

@Data
public class ChannelInfo {
    private String channelId;
    private List<Rtsp> rtsp;
}
