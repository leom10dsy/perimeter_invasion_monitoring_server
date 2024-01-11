package com.csrd.pims.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.csrd.pims.amqp.tk.DefenceState;
import com.csrd.pims.dao.mapper.HuaweiDefenceStateMapper;
import com.csrd.pims.service.HuaweiDefenceStateService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class HuaweiDefenceStateServiceImpl implements HuaweiDefenceStateService {

    @Resource
    private HuaweiDefenceStateMapper defenceStateMapper;

    @Override
    public void updateDefenceState(DefenceState defenceState) {
        defenceStateMapper.updateById(defenceState);
    }

    @Override
    public int insert(DefenceState defenceState) {
        return defenceStateMapper.insert(defenceState);
    }

    @Override
    public DefenceState selectById(String type) {
        return defenceStateMapper.selectById(type);
    }
}
