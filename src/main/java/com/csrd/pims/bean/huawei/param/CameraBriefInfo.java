package com.csrd.pims.bean.huawei.param;

import lombok.Data;

/**
 * 摄像头信息
 */
@Data
public class CameraBriefInfo {
    // 摄像机安装位置 描述：键盘可见字符和中文，长度限制 256字节
    private String cameraLocation;
    // 摄像机扩展状态： ● 1：正常 ● 2：视频丢失
    private String cameraStatus;
    // 摄像机编码
    private String code;
    // 设备创建时间格式为yyyyMMddHHmmss，如20121207102035，长度限制20字节
    private String deviceCreateTime;
    // 主设备类型：● 1：IPC ● 2：DVS ● 3：DVR ● 4：eNVR
    private String deviceFormType;
    // 所属设备组编码 长度限制128字节
    private String deviceGroupCode;
    // 前端IP 点分十进制格式， 例如： 10.166.166.126， 长度限制64字节
    private String deviceIP;
    // 主设备型号 由各设备厂家提供，长度限制32字节
    private String deviceModelType;
    // 设备归属域的域编码,例如：6bdacabae9c546e9ab5b8688ccd85a59，长度限制32字节
    private String domainCode;
    // 是否启用随路音频：● 0：停用 ● 1：启用
    private String enableVoice;
    // 是否为外域：● 0：否 ● 1：是
    private String isExDomain;
    //
    private String isSupportIntelligent;
    //
    private String name;
    // 网络类型：● 0：有线 ● 1：无线
    private String netType;
    // 设备所属NVR编码 表示该设备被该NVR管理，例如：9145a3f7c4164d3ab9e161fcb4221426，长度限制32字节
    private String nvrCode;
    // 主设备编码 例如：32010300100201030000#6bdacabae9c546e9ab5b8688ccd85a59，长度限制64字节
    private String parentCode;
    // 保留字段 长度限制32字节，必须保留该字段，字段内容可以置空
    private String reserve;
    // 设备状态：● 0：离线 ● 1：在线 ● 2：休眠
    private String status;
    // 摄像机类型：● 0：固定枪机 ● 1：有云台枪机 ● 2：球机 ● 3：半球-固定摄像机 ● 4：筒机
    private String type;
    // 设备提供商类型：（长度限制32字节） ● HUAWEI ● HIK ● DAHUA ● SUNELL ● CANON ● CHANGHONG ● TIANDY ● PANASONIC ● AXIS
    private String vendorType;
}
