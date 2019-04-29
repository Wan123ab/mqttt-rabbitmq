package com.wq.mqtttrabbitmq.rabbitmq.service;

import com.wq.mqtttrabbitmq.rabbitmq.bean.MailMessageModel;
import com.wq.mqtttrabbitmq.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService{


    @Resource( name = "rabbitTemplate" )
    private RabbitTemplate rabbitTemplate;

    @Value("${mq.exchange}")
    private String exchange;

    @Value("${mq.routekey}")
    private String routeKey;

    @Value("${mq.ttlroutekey}")
    private String ttlroutekey;

    @Override
    public void sendEmail(MailMessageModel message) throws Exception {
        try {
            //发送端发送消息时，需要指定CorrelationData，用于标识该发送消息的唯一id --- 发送确认（publisher confirms）
            /**
             * 测试消息路由到Queue失败，触发ReturnCallback
             * 控制台打印：
             * ============【消息路由到Queue失败】returnedMessage=========
             * replyCode: 312
             * replyText: NO_ROUTE
             * exchange: email_exchange
             * routingKey: email_routekey1234
             */
//            rabbitTemplate.convertAndSend(exchange, routeKey+"1234", JsonUtils.obj2Json(message), new CorrelationData(message.getId()));
            rabbitTemplate.convertAndSend(exchange, routeKey, JsonUtils.obj2Json(message), new CorrelationData(message.getId() + ""));
        } catch (Exception e) {
            log.error("EmailServiceImpl.sendEmail", ExceptionUtils.getMessage(e));
        }
    }

    /**
     * 发送邮件到TTL延时队列
     * @param message
     * @throws Exception
     */
    @Override
    public void sendEmailTTL(MailMessageModel message) throws Exception {
        try {
            rabbitTemplate.convertAndSend(exchange, ttlroutekey, JsonUtils.obj2Json(message), new CorrelationData(message.getId() + ""));
        } catch (Exception e) {
            log.error("EmailServiceImpl.sendEmailTTL", ExceptionUtils.getMessage(e));
        }

    }
}
