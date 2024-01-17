package com.csrd.pims.bean.huawei;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * ivs视频下载队列
 */
@Data
@TableName("huaweiVideoQueue")
@NoArgsConstructor
@AllArgsConstructor
public class HuaweiVideoQueue {

    @TableId("alarmEventId")
    private String alarmEventId;

    @TableField("cameraNumber")
    private String cameraNumber;

    @TableField("alarmTime")
    private Date alarmTime;

    //上传文件目录
    @TableField("alarmVideo")
    private String alarmVideo;

    // 是否已下载，0：否 1：是
    @TableField("isDownload")
    private int isDownload;

    @TableField("uploadVideoName")
    private String uploadVideoName;


}
