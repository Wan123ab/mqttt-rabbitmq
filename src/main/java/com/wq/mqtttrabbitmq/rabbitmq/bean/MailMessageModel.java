package com.wq.mqtttrabbitmq.rabbitmq.bean;

import lombok.Data;

@Data
public class MailMessageModel {

    private int id;

    private String to;

    private String subject;

    private String text;


}
