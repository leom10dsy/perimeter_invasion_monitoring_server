package com.csrd.pims.bean.web;

import lombok.Data;

@Data
public class HuaweiCallbackVo {

    private String resultCode;

    private String requestXML;

    private String eventData;
}
