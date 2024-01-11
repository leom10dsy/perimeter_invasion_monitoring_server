package com.csrd.pims.bean.huawei.result;

import lombok.Data;

/**
 * 华为光纤信息
 */
@Data
public class HuaweiNCEFiberInfo {

    // 光纤的UUID
    private String fiberUUID;
    // 光纤名称
    private String fiberName;
    // 光纤接入设备的槽位ID
    private String portUUID;
    // 光纤对接到网元的UUID
    private String cardUUID;
    // 光纤接入设备的槽位ID
    private Integer slotId;
    // 光纤接入设备的端口ID
    private Integer portId;
    // 设备能够检测到的光纤的长度
    private Integer monitorLength;
    // 光纤上的光桩总数
    private Integer totalPiles;
    // 用户标记信息
    private String remark;
}
