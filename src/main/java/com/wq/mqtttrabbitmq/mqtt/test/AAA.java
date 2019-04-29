package com.wq.mqtttrabbitmq.mqtt.test;


import com.wq.mqtttrabbitmq.mqtt.config.MqttClientConfig;
import com.wq.mqtttrabbitmq.utils.SpringIOCContainer;

public class AAA {

    private static MqttClientConfig config;

    static{

        config = SpringIOCContainer.getBean("mqttClientConfig",MqttClientConfig.class);

    }

    public static MqttClientConfig getConfig(){
        System.out.println(config);
        return config;
    }

}
