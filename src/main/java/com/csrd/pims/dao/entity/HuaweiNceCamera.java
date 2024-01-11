package com.csrd.pims.dao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("huaweiCamera")
public class HuaweiNceCamera {

    @TableId("cameraNumber")
    public String cameraNumber;

    @TableField("nceIp")
    private String nceIp;

    @TableField("begin")
    private Float begin;

    @TableField("end")
    private Float end;

}
