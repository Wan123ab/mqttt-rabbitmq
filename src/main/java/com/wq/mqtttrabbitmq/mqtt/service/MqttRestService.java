package com.wq.mqtttrabbitmq.mqtt.service;

import com.alibaba.fastjson.JSONObject;
import com.wq.mqtttrabbitmq.mqtt.config.MqttServerConfig;
import com.wq.mqtttrabbitmq.mqtt.constant.MqttConstant;
import com.wq.mqtttrabbitmq.mqtt.constant.QosType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Service
public class MqttRestService {

    @Autowired
    private MqttServerConfig mqttServerConfig;

    private RestTemplate restTemplate;

    @PostConstruct
    public void init() {

        restTemplate = new RestTemplate();
        //添加登录拦截器
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(mqttServerConfig.getUsername(), mqttServerConfig.getPassword()));

    }

    public JSONObject subscribe(String clientId, String topic) throws Exception {
        return subscribe(clientId, QosType.ONLYONCE, topic);
    }

    public JSONObject subscribe(String clientId, QosType qosType, String topic) throws Exception {
        String reqUrl = mqttServerConfig.getBrokerUrl() + MqttConstant.SUBSCRIBE;
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("client_id", clientId);
        jsonParam.put("qos", qosType.getType());
        jsonParam.put("topic", topic);
        return restTemplate.postForObject(reqUrl, jsonParam, JSONObject.class);
    }

    /**
     * 取消订阅
     */
    public JSONObject unSubscribe(String clientId, String topic) throws Exception {
        String reqUrl = mqttServerConfig.getBrokerUrl() + MqttConstant.SUBSCRIBE;
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("topic", topic);
        jsonParam.put("client_id", clientId);
        return restTemplate.postForObject(reqUrl, jsonParam, JSONObject.class);
    }

    /**
     * 获取集群内指定客户端的会话信息
     */
    public JSONObject getClientInfo(String clientId) throws Exception {
        String reqUrl = mqttServerConfig.getBrokerUrl() + MqttConstant.CLIENT_INFO + clientId;
        return restTemplate.getForObject(reqUrl, JSONObject.class);
    }

    /**
     * 断开集群内指定客户端连接
     */
    public JSONObject disConnectClient(String clientId) throws Exception {
        String reqUrl = mqttServerConfig.getBrokerUrl() + MqttConstant.DISCONNECT_CLIENT + clientId;
        return restTemplate.execute(reqUrl, HttpMethod.DELETE, null, new HttpMessageConverterExtractor<JSONObject>(JSONObject.class, restTemplate.getMessageConverters()), (Object) null);
    }

    /**
     * 发布消息
     */
    public void publish(String topic, Object obj) throws Exception {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("topic", topic);
        jsonParam.put("payload", JSONObject.toJSONString(obj));
        jsonParam.put("qos", QosType.ONLYONCE.getType());
        restTemplate.postForObject(mqttServerConfig.getBrokerUrl() + MqttConstant.PUBLISH, jsonParam, JSONObject.class);
    }


}
