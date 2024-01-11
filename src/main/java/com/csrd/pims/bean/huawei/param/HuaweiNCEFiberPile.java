package com.csrd.pims.bean.huawei.param;

import lombok.Data;

/**
 * NCE光桩信息
 */
@Data
public class HuaweiNCEFiberPile {

    //光桩UUID
    private String pileUUID;
    //光桩所属的光纤的UUID
    private String fiberUUID;
    //光桩所属的行政区域信息
    private String address;
    //光纤名称
    private String pileDesc;
    //距设备的距离，单位为m。
    private Double distance;
    //经度
    private Double longitude;
    //纬度
    private Double latitude;
    // 里程
    private String mileage;
    // 站点
    private String stationId;
    // 预制点
    private Integer ptzPoint;

}
