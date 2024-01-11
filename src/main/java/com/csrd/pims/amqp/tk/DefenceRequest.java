package com.csrd.pims.amqp.tk;

import lombok.Getter;
import lombok.Setter;

/**
 * @description: 布撤防下发请求
 * @author: shiwei
 * @create: 2023-11-02 09:50:21
 **/
@Getter
@Setter
public class DefenceRequest {

    private String deviceId;
    private String areaCode;
    private int areaState;
    private String sendTime;

}
