package com.csrd.pims.config.runner;

import com.csrd.pims.amqp.AmqpSender;
import com.csrd.pims.amqp.tk.DefenceState;
import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.bean.config.TkConfigParam;
import com.csrd.pims.dao.mapper.TkAlarmMapper;
import com.csrd.pims.enums.TkSysTypeEnum;
import com.csrd.pims.service.HuaweiDefenceStateService;
import com.csrd.pims.tools.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 初始化参数
 */
@Slf4j
@Component
public class InitRunner implements ApplicationRunner {

    @Resource
    private HuaweiDefenceStateService defenceStateService;

    @Resource
    private HuaweiConfigParam configParam;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        initDefence();
    }

    private void initDefence() {
        if (configParam.getIvs().isEnable()) {
            DefenceState defenceState = defenceStateService.selectById(TkSysTypeEnum.IVS.getType());
            if (Objects.isNull(defenceState)) {
                defenceState = new DefenceState();
                defenceState.setDefence(true);
                defenceState.setType(TkSysTypeEnum.IVS.getType());
                defenceStateService.insert(defenceState);
            }
            Params.IVS_ALARM_ENABLE.set(defenceState.isDefence());
        }

        if (configParam.getNce().isEnable()) {
            DefenceState defenceState = defenceStateService.selectById(TkSysTypeEnum.NCE.getType());
            if (Objects.isNull(defenceState)) {
                defenceState = new DefenceState();
                defenceState.setDefence(true);
                defenceState.setType(TkSysTypeEnum.NCE.getType());
                defenceStateService.insert(defenceState);
            }
            Params.NCE_ALARM_ENABLE.set(defenceState.isDefence());
        }
    }


}
