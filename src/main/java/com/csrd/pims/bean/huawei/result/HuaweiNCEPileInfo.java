package com.csrd.pims.bean.huawei.result;

import lombok.Data;

/**
 * 华为光桩信息
 */
@Data
public class HuaweiNCEPileInfo {
    // 光桩UUID
    private String pileUUID;
    // 光桩所属的光纤的UUID
    private String fiberUUID;
    // 光纤名称
    private String pileDesc;
    // 光桩所属的行政区域信息
    private String address;
    // 距设备的距离，单位为m。
    private Float distance;
    // 经度
    private Float longitude;
    // 纬度
    private Float latitude;
}
