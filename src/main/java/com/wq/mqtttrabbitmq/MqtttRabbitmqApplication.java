package com.wq.mqtttrabbitmq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource(encoding = "UTF-8", value = {"classpath:mqtt.properties"})
public class MqtttRabbitmqApplication {

    public static void main(String[] args) {
        SpringApplication.run(MqtttRabbitmqApplication.class, args);
    }

}
