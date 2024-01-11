package com.csrd.pims.amqp.tk;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("defencestate")
public class DefenceState {
    @TableId("type")
    private String type;
    @TableField("is_defence")
    private boolean isDefence;
}
