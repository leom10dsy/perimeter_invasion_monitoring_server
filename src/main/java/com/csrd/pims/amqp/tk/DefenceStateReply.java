package com.csrd.pims.amqp.tk;

import lombok.Data;

@Data
public class DefenceStateReply{
    private String deviceId;
    private Integer areaState;
    private String areaCode;
    private String time;
}
