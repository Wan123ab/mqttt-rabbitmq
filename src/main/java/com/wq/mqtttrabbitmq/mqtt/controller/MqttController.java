package com.wq.mqtttrabbitmq.mqtt.controller;

import com.wq.mqtttrabbitmq.mqtt.client.MyMqttClient;
import com.wq.mqtttrabbitmq.mqtt.service.MqttRestService;
import com.wq.mqtttrabbitmq.mqtt.test.AAA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MqttController {

    @Autowired
    private MyMqttClient mqttClient;

    @Autowired
    private MqttRestService mqttRestService;


    @GetMapping("/send")
    public void sendMsg(){

        mqttClient.publish("topic1","Hello World!");

//        测试非IOC中的bean静态注入IOC中的bean
//        AAA.getConfig();
    }

    @GetMapping("/sendRest")
    public void sendMsgRest(){

        try {
            mqttRestService.publish("topic1","Hello World Rest!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
