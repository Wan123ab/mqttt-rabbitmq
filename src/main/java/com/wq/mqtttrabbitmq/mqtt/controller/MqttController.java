package com.wq.mqtttrabbitmq.mqtt.controller;

import com.wq.mqtttrabbitmq.mqtt.client.MyMqttClient;
import com.wq.mqtttrabbitmq.mqtt.service.MqttRestService;
import com.wq.mqtttrabbitmq.mqtt.test.AAA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class MqttController {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    /**
     * 测试任务调度，每2s执行一次
     */
//    @Scheduled(fixedRate = 2 * 1000)
    public void sendMsgScheduled(){

        mqttClient.publish("topic1",sdf.format(new Date()));

    }

//    @Scheduled(fixedRate = 2 * 1000)
    public void sendMsgRestScheduled(){

        try {
            mqttRestService.publish("topic2",sdf.format(new Date()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //这是局部异常处理器
//    @ExceptionHandler(Exception.class)
    @GetMapping("/exception")
    public void testException (Exception e){

//        System.out.println(1/0);
        throw new NullPointerException("空指针啦，赶紧去处理！！！");

    }

}
