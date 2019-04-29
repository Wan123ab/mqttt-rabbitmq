package com.wq.mqtttrabbitmq.mqtt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttClientConfig {

    @Value("${mqtt.userName}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.willTopic}")
    private String willTopic;

    @Value("${mqtt.willPayload}")
    private String willPayload;

    @Value("${mqtt.brokerUrl}")
    private String brokerUrl;

    @Value("${mqtt.enableAutoReconn}")
    private boolean enableAutoReconn;

    @Value("${mqtt.connTimeout}")
    private int connTimeout;

    @Value("${mqtt.keepAliveInterval}")
    private int keepAliveInterval;

    @Value("${mqtt.maxReconnTimes}")
    private int maxReconnTimes;

    @Value("${mqtt.reconnInterval}")
    private int reconnInterval;

    @Value("${mqtt.clientPoolSize}")
    private int clientPoolSize;

    @Value("${mqtt.cleanSession}")
    private boolean cleanSession;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public String getWillPayload() {
        return willPayload;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public boolean getEnableAutoReconn() {
        return enableAutoReconn;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public int getMaxReconnTimes() {
        return maxReconnTimes;
    }

    public int getReconnInterval() {
        return reconnInterval;
    }

    public int getClientPoolSize() {
        return clientPoolSize;
    }

    public boolean getCleanSession() {
        return cleanSession;
    }
}
