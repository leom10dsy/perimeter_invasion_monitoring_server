package com.csrd.pims.schedule;


import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.service.HuaweiNceService;
import com.csrd.pims.tools.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Component
public class HuaweiNceSchedule {

    @Resource
    private HuaweiNceService huaweiNceService;

    @Resource
    private HuaweiConfigParam huaweiConfigParam;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void huaweiNceKeepLive() {

        if (huaweiConfigParam.getNce().isEnable() && Params.nceCookie != null &&
                Params.nceSubscriptionResponse != null &&
                DateUtil.between(Params.nceKeepTime, new Date(), DateUnit.SECOND) >= 30) {

            // 取消订阅
            log.warn("超过30s没有收到nce消息 ， 重新订阅");
            Params.nceCookie = null;
            Params.nceRoaRand = null;
            boolean login = huaweiNceService.login(huaweiConfigParam.getNce().getIp(), huaweiConfigParam.getNce().getPort(),
                    huaweiConfigParam.getNce().getUsername(), huaweiConfigParam.getNce().getPassword());
            if (login) {
                huaweiNceService.addSubscription(huaweiConfigParam.getNce().getIp(), huaweiConfigParam.getNce().getPort(),
                        huaweiConfigParam.getNce().getUsername(), huaweiConfigParam.getNce().getPassword());
            } else {
                log.warn("nce connection error");
            }

        }

        if (huaweiConfigParam.getNce().isEnable() && Params.nceCookie == null) {
            boolean login = huaweiNceService.login(huaweiConfigParam.getNce().getIp(), huaweiConfigParam.getNce().getPort(),
                    huaweiConfigParam.getNce().getUsername(), huaweiConfigParam.getNce().getPassword());
            if (login) {
                huaweiNceService.addSubscription(huaweiConfigParam.getNce().getIp(), huaweiConfigParam.getNce().getPort(),
                        huaweiConfigParam.getNce().getUsername(), huaweiConfigParam.getNce().getPassword());
            } else {
                log.warn("nce connection error");
            }
        }
    }
}
