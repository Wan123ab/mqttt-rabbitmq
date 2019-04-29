package com.wq.mqtttrabbitmq.mqtt.client;

import com.alibaba.fastjson.JSONObject;
import com.wq.mqtttrabbitmq.mqtt.config.MqttClientConfig;
import com.wq.mqtttrabbitmq.mqtt.service.MqttRestService;
import com.wq.mqtttrabbitmq.utils.IDUtils;
import com.wq.mqtttrabbitmq.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Component
@Slf4j
public class MyMqttClient {

    @Autowired
    private MqttClientConfig clientConfig;

    @Autowired
    private MqttRestService mqttRestService;

    @Value("${mqtt.maxReconnTimes}")
    private int maxReconnTimes;

    @Value("${mqtt.reconnInterval}")
    private int reconnInterval;

    @Value("${mqtt.clientPoolSize}")
    private int clientPoolSize;

    //MqttClient池
    private List<MqttClient> pool = Collections.synchronizedList(new ArrayList<>());

    /**
     * MQTTClient连接参数
     */
    private MqttConnectOptions mqttConnectOptions;

    private Random random = new Random();

    /**
     * 初始化连接参数MQTTClient和client池子Pool
     */
    @PostConstruct
    public void init(){

        mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(clientConfig.getUsername());
        mqttConnectOptions.setPassword(clientConfig.getPassword().toCharArray());
        mqttConnectOptions.setCleanSession(clientConfig.getCleanSession());
        //设置连接超时5s
        mqttConnectOptions.setConnectionTimeout(clientConfig.getConnTimeout());
        //设置心跳保持时间10s
        mqttConnectOptions.setKeepAliveInterval(clientConfig.getKeepAliveInterval());
        //设置是否自动重连
        mqttConnectOptions.setAutomaticReconnect(clientConfig.getEnableAutoReconn());

        //初始化client池pool
        for (int i = 0; i < clientPoolSize; i++) {
            getClient();
        }
    }


    /**
     * 初始化一个client并放入池中
     * @return
     */
    public MqttClient getClient() {

        if(pool.size() > clientPoolSize){
            log.error("pool池子已满，无法继续添加client！");
        }

        MqttClient mqttClient = null;
        try {
            //注意：创建client需要连接的地址为tcp协议，端口号1883
            mqttClient = new MqttClient(clientConfig.getBrokerUrl(),IDUtils.getId());
            //设置client回调
            if(maxReconnTimes >0 && reconnInterval > 0){
                mqttClient.setCallback(new MyMqttCallBack(this,mqttClient));
            }

            //client连接服务器
            mqttClient.connect(mqttConnectOptions);
            //加入到池中
            pool.add(mqttClient);
        } catch (MqttException e) {
            log.error("初始化client失败，当前clientConfig={}",JsonUtils.obj2Json(clientConfig),e);
        }

        return mqttClient;
    }

    /**
     * 从池中移除指定的client
     * @param mqttClient
     */
    public void removeClient(MqttClient mqttClient){

        if(pool.contains(mqttClient)){
            pool.remove(mqttClient);

        }
    }

    /**
     * 发布消息
     * @param topic
     * @param obj
     */
    public void publish(String topic, Object obj) {
        MqttMessage message = new MqttMessage();
        String payload = JSONObject.toJSONString(obj);
        try {
            message.setPayload(payload.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("message.setPayload error", e);
        }
        message.setQos(2);
        message.setRetained(false);
        MqttClient mqttClient = null;
        try {
            //随机从池子中获取1个client
            mqttClient = pool.get(random.nextInt(pool.size()));

            mqttClient.publish(topic, message);
        } catch (Exception e1) {
            try {
                //调用rest api发送消息
                mqttRestService.publish(topic,message);
            } catch (Exception e) {
                log.error("使用Rest Api发往topic={}失败,payload={}", topic, payload, e);
            }
            log.error("使用MQTTClient发往topic={}失败,payload={}", topic, payload, e1);
        }
    }

    public int getMaxReconnTimes() {
        return maxReconnTimes;
    }

    public int getReconnInterval() {
        return reconnInterval;
    }

    public List<MqttClient> getPool() {
        return pool;
    }

    public MqttConnectOptions getMqttConnectOptions() {
        return mqttConnectOptions;
    }
}
