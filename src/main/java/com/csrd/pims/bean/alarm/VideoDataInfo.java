package com.csrd.pims.bean.alarm;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 慧视视频数据
 */
@Data
@Accessors(chain = true)
@ToString
public class VideoDataInfo {
    private String alarmType;
    private String channelId;
    private String ipv4;
    private String router;
    private List<VideoDataTarget> targets;
    private String time;
}
