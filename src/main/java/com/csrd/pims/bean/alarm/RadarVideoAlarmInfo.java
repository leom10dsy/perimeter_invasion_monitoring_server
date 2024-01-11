package com.csrd.pims.bean.alarm;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 雷视融合报警信息
 */
@TableName("radar_video_alarm_info")
@Data
@Accessors(chain = true)
public class RadarVideoAlarmInfo {

    @TableId("id")
    private String id;
    @TableField("videoId")
    private String videoId;
    @TableField("filePath")
    private String filePath;
    @TableField("isConfirm")
    private int isConfirm;
}
