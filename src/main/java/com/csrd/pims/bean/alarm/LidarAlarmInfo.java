package com.csrd.pims.bean.alarm;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @ClassName: Lidar
 * @Description:
 * @Author: weishuguang
 * @Date: 2022/7/4 14:42
 */

@Data
@Accessors(chain = true)
public class LidarAlarmInfo implements Serializable{

    private String id;

    /**
     *  设备类型
     */
    private String typeCode;

    /**
     *  设备 ID
     */
    private String equipmentID;

    /**
     *  路局集团公司编码
     */
    private String companyCode;

    /**
     *  线路编码
     */
    private String lineCode;

    /**
     *  横向坐标
     */
    private Float xCoordinate;

    /**
     *  纵向坐标
     */
    private Float yCoordinate;

    /**
     *  高度坐标
     */
    private Float zCoordinate;

    /**
     *  反射率
     */
    private Float reflectivity;

    /**
     *  采集时间
     */
    private String time;

    /**
     *  是否删除
     */
    private String isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
