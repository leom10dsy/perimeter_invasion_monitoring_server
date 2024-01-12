package com.csrd.pims.schedule;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.csrd.pims.bean.huawei.bean.HWAlarmInfo;
import com.csrd.pims.service.HuaweiIvsService;
import com.csrd.pims.tools.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Component
public class AlarmStateSchedule {

    @Resource
    private HuaweiIvsService ivsService;

    /**
     * 超过10秒未收到报警消息，推送结束报警
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void alarmState() {
        if (Params.LATEST_ALARM_TIME.isEmpty()) {
            return;
        }
        Date nowDate = new Date();
        for (String mapKey : Params.LATEST_ALARM_TIME.keySet()) {
            HWAlarmInfo hwAlarmInfo = Params.LATEST_ALARM_TIME.get(mapKey);
            Date latestTime = hwAlarmInfo.getAlarmTime();
            long between = DateUtil.between(latestTime, nowDate, DateUnit.SECOND);
            if (between > 10) {
                // 推送结束报警并入库
                try {
                    ivsService.pushCloseAlarm(mapKey, nowDate);
                } catch (Exception e) {
                    log.error("=====> 推送结束报警错误，alarmEventID:{}", hwAlarmInfo.getEventId());
                    log.error(e.getMessage());
                }
                Params.LATEST_ALARM_TIME.remove(mapKey);
            }
        }

    }
}
