package com.wq.mqtttrabbitmq.rabbitmq.client;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class RabbitClient {

    @Autowired
    private RabbitTemplate tmp;

    private static RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate = tmp;
    }

    public static <T> void send(String exchange,String routeKey, T t){

        rabbitTemplate.convertAndSend(exchange, routeKey, t);
    }


}
