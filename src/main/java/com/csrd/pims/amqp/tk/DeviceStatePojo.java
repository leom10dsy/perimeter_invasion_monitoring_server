package com.csrd.pims.amqp.tk;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @description:
 * @author: shiwei
 * @create: 2023-11-01 14:14:25
 **/
@Getter
@Setter
@Accessors(chain = true)
public class DeviceStatePojo {

    private String companyCode;
    private String deviceId;
    private int deviceState;
    private List<DefenceAreaPojo> area;
    private String sendTime;

    // 故障原因  0 无异常 1激光雷达故障 2毫米波雷达故障 3摄像头故障 4盒子故障 5 震动光纤多个使用,分割,初始化0
    private String failureCause;
}
