package com.csrd.pims.amqp.listener;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.csrd.pims.amqp.AmqpSender;
import com.csrd.pims.amqp.tk.DefenceRequest;
import com.csrd.pims.amqp.tk.DefenceState;
import com.csrd.pims.amqp.tk.DefenceStateReply;
import com.csrd.pims.amqp.tk.DeviceStateReply;
import com.csrd.pims.bean.config.TkConfigParam;
import com.csrd.pims.enums.TkSysTypeEnum;
import com.csrd.pims.service.HuaweiDefenceStateService;
import com.csrd.pims.tools.GsonUtil;
import com.csrd.pims.tools.Params;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @description: 铁科下发数据
 * @author: shiwei
 * @create: 2023-11-01 17:06:59
 **/
@Component
@Slf4j
public class TkReplyListener {

    @Resource
    private AmqpSender amqpSender;
    @Resource
    private TkConfigParam tkConfigParam;
    @Resource
    private HuaweiDefenceStateService defenceStateService;

    @RabbitListener(queues = "${tk.amq.alarm_fanout_queue}")
    public void alarmReply(String msg) {
        if(msg.contains("_"+tkConfigParam.getBase().getCompanyCode())){
        log.info("----> 收到平台报警答复:{}", msg);
        }
    }


    @RabbitListener(queues = "${tk.amq.state_fanout_queue}")
    public void deviceStateReply(String msg) {
        try {
            DeviceStateReply deviceStateReply = GsonUtil.fromJson(msg, DeviceStateReply.class);
            if (deviceStateReply != null && StringUtils.equals(tkConfigParam.getBase().getIvsDeviceId(), deviceStateReply.getDeviceId())) {
                log.info("----> ivs收到平台设备状态答复:{}", msg);
            }
            if (deviceStateReply != null && StringUtils.equals(tkConfigParam.getBase().getNceDeviceId(), deviceStateReply.getDeviceId())) {
                log.info("----> nce收到平台设备状态答复:{}", msg);
            }
        } catch (Exception e) {
            log.error("deviceStateReply exception message", e);
        }
    }


    @RabbitListener(queues = "${tk.amq.command_fanout_queue}")
    public void defenceCmd(String msg) {
        try {
            DefenceRequest defenceRequest = GsonUtil.fromJson(msg, DefenceRequest.class);
            if (defenceRequest != null && StringUtils.equals(tkConfigParam.getBase().getIvsDeviceId(), defenceRequest.getDeviceId())) {
                log.info("=====> 收到ivs布撤防:{}",defenceRequest);
                Params.IVS_ALARM_ENABLE.set(defenceRequest.getAreaState() == 1);
                handleDefence(defenceRequest, TkSysTypeEnum.IVS.getType());
            } else if (defenceRequest != null && StringUtils.equals(tkConfigParam.getBase().getNceDeviceId(), defenceRequest.getDeviceId())) {
                Params.NCE_ALARM_ENABLE.set(defenceRequest.getAreaState() == 1);
                log.info("=====> 收到nce布撤防:{}",defenceRequest);
                handleDefence(defenceRequest, TkSysTypeEnum.NCE.getType());
            }
        } catch (Exception e) {
            log.error("defenceCmd exception message", e);
        }
    }

    private void handleDefence(DefenceRequest defenceRequest, String type) {
        log.info("---> {}收到平台布撤防指令:{} 平台指令下发时间:{}", type, defenceRequest.getAreaState(), defenceRequest.getSendTime());
        // 更新布撤防
        log.info("---> 更新{}布撤防指令成功，当前状态:{}", type, defenceRequest.getAreaState());
        log.info("---> {}处理结束 ** 通知平台,发送确认布撤防信息 **", type);
        DefenceState defenceState = new DefenceState();
        defenceState.setDefence(defenceRequest.getAreaState() == 1);
        defenceState.setType(type);
        defenceStateService.updateDefenceState(defenceState);

        DefenceStateReply defenceStateReply = new DefenceStateReply();
        defenceStateReply.setAreaCode(defenceRequest.getAreaCode());
        defenceStateReply.setTime(DateUtil.format(new Date(), DatePattern.NORM_DATETIME_PATTERN));
        defenceStateReply.setAreaState(defenceRequest.getAreaState());
        defenceStateReply.setDeviceId(defenceRequest.getDeviceId());
        amqpSender.sendByRouter(tkConfigParam.getAmq().getCommandFanoutExchange(), tkConfigParam.getAmq().getCommandConfirmQueue(), GsonUtil.toJson(defenceStateReply));
    }
}
