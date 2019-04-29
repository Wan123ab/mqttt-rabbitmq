package com.wq.mqtttrabbitmq.rabbitmq.service;

import com.wq.mqtttrabbitmq.rabbitmq.bean.MailMessageModel;

public interface EmailService {

    /**
     * 发送邮件任务存入消息队列
     * @param mailMessageModel
     * @throws Exception
     */
    void sendEmail(MailMessageModel mailMessageModel) throws Exception;

    /**
     * 发送邮件任务存入TTL延时队列
     * @param mailMessageModel
     * @throws Exception
     */
    void sendEmailTTL(MailMessageModel mailMessageModel) throws Exception;

    /**
     * 发送邮件任务到优先级队列
     * @param message
     * @throws Exception
     */
    void sendEmailPriority(MailMessageModel message) throws Exception;
}
