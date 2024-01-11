package com.csrd.pims.config;

import com.csrd.pims.tools.Params;
import com.csrd.pims.tools.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @description: 系统全局参数
 * @author: shiwei
 * @create: 2022-07-06 17:55:55
 **/
@Configuration
@Slf4j
public class SystemParamConfig {

    @Value("${sysconfig.local-media-path}")
    private String localMediaPath;

    @Value("${sysconfig.local-delete-flag}")
    private Boolean localDeleteFlag;

    @Value("${sysconfig.local.ip}")
    private String localIp;


    @Bean
    public void initSystemParams() {
        Params.LOCAL_STORAGE_PATH = Utils.getStandardFilePath(localMediaPath) + File.separator;
        Params.LOCAL_DELETE_FLAG = localDeleteFlag;
        Params.LOCAL_IP = localIp;
        log.info("LOCAL STORAGE PATH:{}", Params.LOCAL_STORAGE_PATH);

    }


    @Bean
    public void cleanLogFile() {
        Utils.cleanJvmErrLog();
    }

}
