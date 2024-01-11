package com.csrd.pims.amqp;

import com.csrd.pims.bean.config.TkConfigParam;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AmqpTest {
    @Autowired
    private AmqpSender amqpSender;
    @Autowired
    private TkConfigParam tkConfigParam;

    @Test
    public void sendTest01() {
        amqpSender.sendByQueue(tkConfigParam.getAmq().getAlarmMergeQueue(), "=======> 是不是能够成功推送到交换机和路由");
    }

    @Test
    public void sendTest02() {
        amqpSender.sendByRouter(tkConfigParam.getAmq().getTestMonitorPlatform(), tkConfigParam.getAmq().getAlarmMergeRoutingKey(), "=======> 是不是能够成功推送到交换机和路由");
    }
}
