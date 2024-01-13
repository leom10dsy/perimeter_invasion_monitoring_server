package com.csrd.pims.config.huawei;

import com.csrd.pims.bean.config.HuaweiConfigParam;
import com.csrd.pims.socket.huawei.radar.HuaweiNettyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class HuaweiRadarConfig implements CommandLineRunner {

    @Autowired
    private HuaweiConfigParam huaweiConfigParam;


    // 故障原因  0 无异常 1激光雷达故障 2毫米波雷达故障 3摄像头故障 4盒子故障 5 震动光纤多个使用,分割,初始化0
    public static ConcurrentHashMap<String, String> FAILURE_CAUSE = new ConcurrentHashMap<>();

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
