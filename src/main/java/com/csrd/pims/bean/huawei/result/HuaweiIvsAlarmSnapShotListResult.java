package com.csrd.pims.bean.huawei.result;

import lombok.Data;

import java.util.List;

@Data
public class HuaweiIvsAlarmSnapShotListResult {
    //成功返回0，失败返回错误码
    private int resultCode;
    //抓拍图片列表
    private List<SnapShotInfo> snapShotList;

    @Data
    public static class SnapShotInfo {
        //摄像机编码
        private String cameraCode;
        //抓拍图片ID
        private int pictureId;
        //抓拍图片文件名
        private String pictureName;
        //图片文件大小，单位KB
        private int pictureSize;
        //抓拍图片URL
        private String pictureUrl;
        //抓拍缩略图URL
        private String previewUrl;
        //抓拍时间(UTC时间)，yyyyMMddHHmmss
        private String snapTime;
        //抓拍类型：1：智能分析抓拍2：告警抓拍4：手动抓拍(包括定时抓拍)
        private int snapType;
    }
}
