package com.csrd.pims.dao.entity.ivs;

import lombok.Data;

/**
 * @ClassName: HuaWeiCamera
 * @Description:
 * @Author: weishuguang
 * @Date: 2022/10/17 11:27
 */
@Data

public class HuaweiCamera {

    /**
     * 摄像头编号
     */
    private String number;

    /**
     * ivs编号
     */
    private String ivsNumber;

    /**
     * ivs名称
     */
    private String ivsName;

    /**
     * 摄像头IP
     */
    private String cameraIp;

    /**
     * 摄像头名称
     */
    private String cameraName;

    /**
     * 摄像头厂商
     */
    private String cameraType;

    /**
     * scada画图工具对应图标类型id
     */
    private String scadaNodeId;

    /**
     * 设备状态
     */
    private Integer state;

    /**
     * 是否智能订阅
     */
    private Integer subscribeFlag;

    private String subscribeID;

}
