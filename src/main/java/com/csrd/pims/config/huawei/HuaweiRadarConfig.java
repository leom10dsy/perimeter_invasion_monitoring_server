package com.csrd.pims.config.huawei;

import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.socket.huawei.radar.HuaweiNettyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class HuaweiRadarConfig implements CommandLineRunner {

    @Autowired
    private HuaweiConfigParam huaweiConfigParam;


    /**
     * 登录radr
     */
    private void login() {
        new HuaweiNettyClient().start();
        log.info("=====> 连接华为雷达！！！");
    }


    @Override
    public void run(String... args) throws Exception {
        if (huaweiConfigParam.getRadar().isEnable()) {
            login();
        }
    }
}
