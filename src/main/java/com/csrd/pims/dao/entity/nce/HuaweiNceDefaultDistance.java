package com.csrd.pims.dao.entity.nce;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("defaultdistance")
public class HuaweiNceDefaultDistance {

    @TableId("cameraNumber")
    private String cameraNumber;

    @TableField("distance")
    private String distance;
}
