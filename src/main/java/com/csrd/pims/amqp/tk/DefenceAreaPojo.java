package com.csrd.pims.amqp.tk;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @description:
 * @author: shiwei
 * @create: 2023-11-01 14:15:45
 **/
@Getter
@Setter
@Accessors(chain = true)
public class DefenceAreaPojo {
    private String areaCode;
    private int areaState;
}
