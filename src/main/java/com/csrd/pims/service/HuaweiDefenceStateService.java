package com.csrd.pims.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.csrd.pims.amqp.tk.DefenceState;

public interface HuaweiDefenceStateService{
    void updateDefenceState(DefenceState defenceState);
    int insert(DefenceState defenceState);
    DefenceState selectById(String type);
}
