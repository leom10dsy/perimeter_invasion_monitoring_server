package com.csrd.pims.bean.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "tk")
public class TkConfigParam {


    private Base base = new Base();

    private Sftp sftp = new Sftp();

    private Amq amq = new Amq();

    @Data
    public class Base {

        private String companyCode;

        private String ivsalgorithmCode;

        private String ncealgorithmCode;

        private String ivsAreaCode;

        private String nceAreaCode;

        private String companyName;

        private String ivsPosition;

        private String ncePosition;

        private String nceDistance;

        private String ivsLineInfo;
        private String ivsDeviceId;
        private String ivsCameraChannelId;
        private String ivsCameraDeviceId;

        private String nceLineInfo;
        private String nceDeviceId;
        private String nceCameraChannelId;
        private String nceCameraDeviceId;

    }

    @Data
    public class Sftp {
        private String username;

        private String password;

        private String host;

        private int port;

        private String videoPath;

        private String imagePath;

    }

    @Data
    public class Amq {
        // 交换机
        private String testMonitorPlatform;

        // 路由
        private String alarmMergeRoutingKey;
        private String stateMergeRoutingKey;
        private String commandRoutingKey;

        // 绑定路由队列
        private String alarmMergeQueue;
        private String stateMergeQueue;
        private String commandConfirmQueue;

        // 广播队列交换机
        private String alarmFanoutExchange;
        private String stateFanoutExchange;
        private String commandFanoutExchange;

        //自定义广播队列
        private String alarmFanoutQueue;
        private String stateFanoutQueue;
        private String commandFanoutQueue;

        private String videoPath;

        private String imagePath;
    }


}
