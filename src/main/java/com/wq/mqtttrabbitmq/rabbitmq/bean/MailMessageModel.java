package com.wq.mqtttrabbitmq.rabbitmq.bean;

import lombok.Data;

@Data
public class MailMessageModel {

    private int id;

    private String to;

    private String subject;

    private String text;
    /**
     * 消息优先级
     */
    private int level;


}
