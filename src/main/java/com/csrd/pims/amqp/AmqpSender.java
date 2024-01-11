package com.csrd.pims.amqp;


import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 监听服务器发送的指令
 * Created by shiwei on 2019/8/25.
 */
@Component
public class AmqpSender {

    @Autowired
    private AmqpTemplate template;

    private final static MessagePostProcessor messagePostProcessor = message -> {
        message.getMessageProperties().setContentType("application/json");
        message.getMessageProperties().setContentEncoding("UTF-8");
        return message;
    };

    // 指定路由发送消息
    public void sendByRouter(String exchange, String routerKey, Object message) {
        template.convertAndSend(exchange, routerKey, message, messagePostProcessor);
    }

    // 指定queue发送消息
    public void sendByQueue(String queue, Object message) {
        template.convertAndSend(queue, message, messagePostProcessor);
    }

}
