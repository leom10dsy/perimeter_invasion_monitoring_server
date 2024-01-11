package com.csrd.pims.amqp;

import com.csrd.pims.bean.config.TkConfigParam;
import com.csrd.pims.dao.mapper.TkAlarmMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * rabbitmq configuration
 * Created by shiwei on 2019/08/25
 */
@Configuration
@Slf4j
public class AmqpConfig {

    @Resource
    private TkConfigParam tkConfigParam;
    @Resource
    private TkAlarmMapper tkAlarmMapper;
    @Resource
    private AmqpSender amqpSender;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 交换机
     */
    @Bean
    public DirectExchange testDirectExchange() {
        return new DirectExchange(tkConfigParam.getAmq().getTestMonitorPlatform(), true, true);
    }

    @Bean
    public FanoutExchange alarmReplyExchange() {
        return new FanoutExchange(tkConfigParam.getAmq().getAlarmFanoutExchange(), true, false);
    }

    @Bean
    public FanoutExchange stateReplyExchange() {
        return new FanoutExchange(tkConfigParam.getAmq().getStateFanoutExchange(), true, false);
    }

    @Bean
    public FanoutExchange defenceReplyExchange() {
        return new FanoutExchange(tkConfigParam.getAmq().getCommandFanoutExchange(), true, false);
    }

    /**
     * 创建queue
     */
    @Bean
    public Queue alarmMergeQueue() {
        return new Queue(tkConfigParam.getAmq().getAlarmMergeQueue(), true);
    }

    @Bean
    public Queue stateQueueMerge() {
        return new Queue(tkConfigParam.getAmq().getStateMergeQueue(), true);
    }

    @Bean
    public Queue commandQueueMerge() {
        return new Queue(tkConfigParam.getAmq().getCommandConfirmQueue(), true);
    }

    @Bean
    public Queue alarmFanoutQueue() {
        return new Queue(tkConfigParam.getAmq().getAlarmFanoutQueue(), true);
    }

    @Bean
    public Queue stateFanoutQueue() {
        return new Queue(tkConfigParam.getAmq().getStateFanoutQueue(), true);
    }

    @Bean
    public Queue commandFanoutQueue() {
        return new Queue(tkConfigParam.getAmq().getCommandFanoutQueue(), true);
    }

    // 绑定routeKey
    @Bean
    public Binding alarmMergeBinding() {
        return BindingBuilder.bind(alarmMergeQueue()).to(testDirectExchange()).with(tkConfigParam.getAmq().getAlarmMergeRoutingKey());
    }

    @Bean
    public Binding stateMergeBinding() {
        return BindingBuilder.bind(stateQueueMerge()).to(testDirectExchange()).with(tkConfigParam.getAmq().getStateMergeRoutingKey());
    }

    @Bean
    public Binding commandMergeBinding() {
        return BindingBuilder.bind(commandQueueMerge()).to(testDirectExchange()).with(tkConfigParam.getAmq().getCommandRoutingKey());
    }


    @Bean
    public Binding alarmFanoutPlatBinding() {
        return BindingBuilder.bind(alarmFanoutQueue()).to(alarmReplyExchange());
    }

    @Bean
    public Binding stateFanoutPlatBinding() {
        return BindingBuilder.bind(stateFanoutQueue()).to(stateReplyExchange());
    }

    @Bean
    public Binding commandFanoutPlatBinding() {
        return BindingBuilder.bind(commandFanoutQueue()).to(defenceReplyExchange());
    }

    @Bean
    public void setConListener() {
        ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();
        connectionFactory.addConnectionListener(new RabbitConnectionListener(tkAlarmMapper, tkConfigParam, amqpSender));
    }


}
