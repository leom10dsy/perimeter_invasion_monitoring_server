package com.csrd.pims.config.huawei;

import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.service.HuaweiNceService;
import com.csrd.pims.tools.Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class HuaweiNceConfig implements CommandLineRunner {

    @Autowired
    private HuaweiNceService huaweiNceService;

    @Resource
    private HuaweiConfigParam huaweiConfigParam;


    /**
     * 登录nce
     */
    private void login() {
        while (!huaweiNceService.login(huaweiConfigParam.getNce().getIp(), huaweiConfigParam.getNce().getPort(),
                huaweiConfigParam.getNce().getUsername(), huaweiConfigParam.getNce().getPassword())) {
            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.error("=====> nce login error retry...");
            login();
        }
        log.info("=====> nce login success");
        // 订阅报警(默认建立长连接)
        while (!huaweiNceService.addSubscription(huaweiConfigParam.getNce().getIp(),
                huaweiConfigParam.getNce().getPort(),
                Params.nceCookie, Params.nceRoaRand)) {
            try {
                Thread.sleep(5 * 60 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        log.info("=====> nce subscription success");
    }


    @Override
    public void run(String... args) throws Exception {
        if (huaweiConfigParam.getNce().isEnable()) {
            new Thread(this::login).start();
        }
    }
}
