package com.csrd.pims.bean.alarm;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 保存雷视融合视频图片地址
 */
@Data
@Accessors(chain = true)
public class RadarVideoFilePath {
    List<String> videoPaths;
    List<String> imagePaths;
}
