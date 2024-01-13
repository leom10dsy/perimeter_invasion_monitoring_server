package com.csrd.pims.config;

import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.dao.entity.ivs.HuaweiCamera;
import com.csrd.pims.service.HuaweiIvsService;
import com.csrd.pims.service.HuaweiNceService;
import com.csrd.pims.tools.Params;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 停止服务时注销登录
 */

@Component
public class PIMSDestroyConfig implements DisposableBean {

    @Resource
    private HuaweiIvsService huaweiIvsService;
    @Resource
    private HuaweiNceService huaweiNceService;

    @Autowired
    private HuaweiConfigParam huaweiConfigParam;

    @Override
    public void destroy() {

        if (huaweiConfigParam.getIvs().isEnable() && Params.ivsCookie != null) {
            //去订阅
            for (HuaweiCamera huaweiCamera : Params.huaweiCameras) {
                huaweiIvsService.deleteSubscribeIntelligentAlarm(huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(),
                        Params.ivsCookie, huaweiCamera.getSubscribeID());
                huaweiCamera.setSubscribeFlag(0);
                huaweiCamera.setState(4);
            }

            huaweiIvsService.logout(huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(),
                    Params.ivsCookie);
            if (Params.FAILURE_CAUSE.containsKey("ivs")){
                Params.FAILURE_CAUSE.remove("ivs");
            }

        }

        if (huaweiConfigParam.getNce().isEnable() && Params.nceCookie != null) {
            huaweiNceService.logout(huaweiConfigParam.getNce().getIp(), huaweiConfigParam.getNce().getPort(),
                    Params.nceCookie, Params.nceRoaRand);
        }

    }
}

