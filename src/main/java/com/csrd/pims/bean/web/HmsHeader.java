package com.csrd.pims.bean.web;

import lombok.Data;

/**
 * 需要用到请求的header参数
 */
@Data
public class HmsHeader {
    private String Cookie;
    private String ContentType;
    private String X_Auth_Token;

    public HmsHeader() {
    }

    public HmsHeader(String cookie, String contentType) {
        Cookie = cookie;
        ContentType = contentType;
    }
}
