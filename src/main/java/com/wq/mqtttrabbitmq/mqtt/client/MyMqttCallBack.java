package com.wq.mqtttrabbitmq.mqtt.client;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;

import java.util.concurrent.TimeUnit;

/**
 * MQTT消息回调接口
 */
@Slf4j
public class MyMqttCallBack implements MqttCallback {

    private MyMqttClient myMqttClient;

    private MqttClient mqttClient;

    public MyMqttCallBack() {
    }

    public MyMqttCallBack(MyMqttClient myMqttClient,MqttClient mqttClient) {
        this.myMqttClient = myMqttClient;
        this.mqttClient = mqttClient;
    }

    /**
     * 断线重连
     * @param cause
     */
    @Override
    public void connectionLost(Throwable cause) {
        System.out.println("连接异常断开,开始尝试重新连接");
        cause.printStackTrace();
        int times = 0;

        do{
            try {
                mqttClient.reconnect();
                times ++ ;
                TimeUnit.SECONDS.sleep(myMqttClient.getReconnInterval());
            } catch (MqttException e) {
                log.error("client重新连接服务器异常",e);
            } catch (InterruptedException e) {
                log.error("线程休眠异常",e);
            }

        }while (!mqttClient.isConnected() && times <= myMqttClient.getMaxReconnTimes());

        //如果还是连接失败，从池中剔除掉该client并重新获取1个新的client放入池中
        if(!mqttClient.isConnected()){
            myMqttClient.removeClient(mqttClient);
            myMqttClient.getClient();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        System.out.println("有消息过来啦，"+message.getPayload().toString());

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
