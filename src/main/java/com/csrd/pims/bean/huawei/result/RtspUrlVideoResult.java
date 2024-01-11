package com.csrd.pims.bean.huawei.result;

public class RtspUrlVideoResult {
    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getRtspURL() {
        return rtspURL;
    }

    public void setRtspURL(String rtspURL) {
        this.rtspURL = rtspURL;
    }

    private int resultCode;
    private String rtspURL;
}
