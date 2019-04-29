package com.wq.mqtttrabbitmq.mqtt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttServerConfig {

    @Value("${mqtt.server.username}")
    private String username;

    @Value("${mqtt.server.password}")
    private String password;


    @Value("${mqtt.server.restBrokerUrl}")
    private String brokerUrl;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }
}
