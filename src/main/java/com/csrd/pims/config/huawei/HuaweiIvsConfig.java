package com.csrd.pims.config.huawei;

import com.csrd.pims.amqp.AmqpSender;
import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.dao.entity.ivs.HuaweiCamera;
import com.csrd.pims.service.HuaweiIvsService;
import com.csrd.pims.tools.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class HuaweiIvsConfig implements CommandLineRunner {


    @Autowired
    private HuaweiIvsService huaweiIvsService;

    @Resource
    private HuaweiConfigParam huaweiConfigParam;

    @Resource
    private AmqpSender amqpSender;


    /**
     * 登录ivs
     */
    private void login() {
        boolean login = huaweiIvsService.login(huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(),
                huaweiConfigParam.getIvs().getUsername(), huaweiConfigParam.getIvs().getPassword());

        if (login) {

            log.info("=====> ivs login success");
            List<HuaweiCamera> huaweiCameras = huaweiIvsService.getAllCameraByIpAndPort();

            for (HuaweiCamera huaweiCamera : huaweiCameras) {
                //订阅智能报警
                String subscribeID = huaweiIvsService.addSubscribeIntelligentAlarm(huaweiConfigParam.getIvs().getIp(), huaweiConfigParam.getIvs().getPort(),
                        Params.ivsCookie, huaweiCamera.getNumber());
                huaweiCamera.setSubscribeFlag(1);
                huaweiCamera.setSubscribeID(subscribeID);
            }

            Params.huaweiCameras = huaweiCameras;

            log.info("camera subscription successfully {}", Params.huaweiCameras);


        } else {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("=====> ivs login error retry...");
            login();
        }

    }

    @Override
    public void run(String... args) throws Exception {
        if (huaweiConfigParam.getIvs().isEnable()) {
            new Thread(this::login).start();
        }
    }
}
