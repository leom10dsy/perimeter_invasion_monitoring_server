package com.csrd.pims.bean.huawei.param;

public class RtspUrlApiModel {
    public String getCameraCode() {
        return cameraCode;
    }

    public void setCameraCode(String cameraCode) {
        this.cameraCode = cameraCode;
    }

    public MediaURL getMediaURLParam() {
        return mediaURLParam;
    }

    public void setMediaURLParam(MediaURL mediaURLParam) {
        this.mediaURLParam = mediaURLParam;
    }

    private String  cameraCode;
    private MediaURL mediaURLParam;
}
