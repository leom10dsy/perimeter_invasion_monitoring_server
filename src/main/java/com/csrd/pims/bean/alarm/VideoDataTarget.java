package com.csrd.pims.bean.alarm;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 慧视视频数据target
 */
@Data
@Accessors(chain = true)
@ToString
public class VideoDataTarget {
    private String score;
    private String targetId;
    private String targetType;
    private String targetDistance;
    private String targetHeight;
    private String targetWidth;
    private String targetOrientation;
    private String targetX;
    private String targetY;
    private String targetXSpeed;
    private String targetYSpeed;
}
