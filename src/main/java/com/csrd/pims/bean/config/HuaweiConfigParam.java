package com.csrd.pims.bean.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "huawei-config")
public class HuaweiConfigParam {

    private Ivs ivs = new Ivs();
    private Nce nce = new Nce();
    private Radar radar = new Radar();

    @Data
    public class Ivs {
        private boolean enable;
        private boolean download;
        private String number;
        private String ip;
        private String port;
        private String username;
        private String password;
    }

    @Data
    public class Nce {
        private boolean enable;
        private String ip;
        private String port;
        private String username;
        private String password;
        private String ptzPointDistance;
        private String cameraIp;
        private Integer timeSpanSec;
        private Integer timeInterval;
        private Boolean isSingle;
    }

    @Data
    public class Radar {
        private boolean enable;
        private String ip;
        private int port;
        private String username;
        private String password;
    }
}
