package com.csrd.pims.dao.entity.nce;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("nceAlarmDistance")
public class HuaweiNceAlarmDistance {

    @TableId("id")
    private Integer id;

    @TableField("begin")
    private Integer begin;

    @TableField("end")
    private Integer end;

    @TableField("coefficient")
    private Float coefficient;

    @TableField("baseDistance")
    private Float baseDistance;

    @TableField("type")
    private Integer type;

}
