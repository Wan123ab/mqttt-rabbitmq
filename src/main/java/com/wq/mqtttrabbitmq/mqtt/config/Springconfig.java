package com.wq.mqtttrabbitmq.mqtt.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;

/**
 * 自定义的MQTT生产端和消费端，用于测试(监听client有没有成功发送消息)
 */
@Configuration
@IntegrationComponentScan
public class Springconfig {

    private Logger logger = LoggerFactory.getLogger(Springconfig.class);

    @Value("${mqtt.userName}")
    private String username;

    @Value("${mqtt.password}")
    private String password;

    @Value("${mqtt.brokerUrl}")
    private String url;

    @Value("${mqtt.clientId}")
    private String clientId;

    @Value("${mqtt.defaultTopic}")
    private String defaultTopic;

    @Value("${mqtt.connTimeout}")
    private int connectionTimeout;

    @Value("${mqtt.keepAliveInterval}")
    private int keepAliveInterval;

    @Value("${mqtt.completionTimeout}")
    private int completionTimeout;

    /** 遗嘱消息 */
    private static final byte[] WILL_DATA;

    static {

        WILL_DATA = "offline".getBytes();
    }


    /********************************** MQTT消费端 ***********************************************/

    @Bean
    public MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，
        // 这里设置为true表示每次连接到服务器都以新的身份连接
        options.setCleanSession(true);
        // 设置连接的用户名
        options.setUserName(username);
        // 设置连接的密码
        options.setPassword(password.toCharArray());
        //options.setServerURIs(StringUtils.split(url, ","));
        options.setServerURIs(new String[]{url});
        // 设置超时时间 单位为秒
        options.setConnectionTimeout(connectionTimeout);
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送心跳判断客户端是否在线，但这个方法并没有重连的机制
        options.setKeepAliveInterval(keepAliveInterval);
        // 设置“遗嘱”消息的话题，若客户端与服务器之间的连接意外中断，服务器将发布客户端的“遗嘱”消息。
        options.setWill("willTopic", WILL_DATA, 2, false);
        return options;
    }

    /*****
     * 创建MqttPahoClientFactory，设置MQTT Broker连接属性，如果使用SSL验证，也在这里设置。
     * @return
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(getMqttConnectOptions());

        return factory;
    }

    /**
     * 接收通道
     * @return
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 配置客户端监听主题
     * @return
     */
    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(clientId + "_inbound",
                mqttClientFactory(), "topic1", "topic2","sysLog",defaultTopic);
        adapter.setCompletionTimeout(completionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        //设置订阅通道
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    //ServiceActivator注解表明当前方法用于处理MQTT消息，inputChannel参数指定了用于接收消息信息的channel。
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            String payload = message.getPayload().toString();
            String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
            // 根据topic分别进行消息处理。
            if (topic.equals("topic1")) {
                logger.info("客户端1监听到topic1消息={}", payload);
            } else if (topic.equals("topic2")) {
                logger.info("客户端1监听到topic2消息={}", payload);
            } else if (topic.equals(defaultTopic)) {
                logger.info("客户端2监听到defaultTopic日志消息={}", payload);
            } else if (topic.equals("sysLog")) {
                logger.info("客户端1监听到sysLog日志消息={}", payload);
            } else {
                System.out.println(topic + ": 丢弃消息 " + payload);
            }
        };
    }

    /**
     * MQTT信息通道（消费者）
     * @return
     */
    @Bean
    public MessageChannel mqttInputChannel1() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound1() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId+"_inbound1", mqttClientFactory(),
                        "topic1", "topic2","sysLog",defaultTopic);
        adapter.setCompletionTimeout(completionTimeout);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        //设置订阅通道
        adapter.setOutputChannel(mqttInputChannel1());
        return adapter;
    }

    @Bean
    //ServiceActivator注解表明当前方法用于处理MQTT消息，inputChannel参数指定了用于接收消息信息的channel。
    @ServiceActivator(inputChannel = "mqttInputChannel1")
    public MessageHandler handler1() {
        return message -> {
            String payload = message.getPayload().toString();
            String topic = message.getHeaders().get("mqtt_receivedTopic").toString();

            // 根据topic分别进行消息处理。
            if (topic.equals("topic1")) {
                logger.info("客户端2监听到topic1消息={}",payload);
            } else if (topic.equals("topic2")) {
                logger.info("客户端2监听到topic2消息={}",payload);
            } else if (topic.equals("sysLog")) {
                logger.info("客户端2监听到sysLog日志消息={}",payload);
            }else if (topic.equals(defaultTopic)) {
                logger.info("客户端2监听到defaultTopic日志消息={}",payload);
            } else {
                System.out.println(topic + ": 丢弃消息 " + payload);
            }
        };
    }



    /********************************** MQTT发送端 ***********************************************/
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /*****
     * 发送消息和消费消息Channel可以使用相同MqttPahoClientFactory
     * @return
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler outbound() {
        // 在这里进行mqttOutboundChannel的相关设置
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler("publishClient", mqttClientFactory());
        messageHandler.setAsync(true); //如果设置成true，发送消息时将不会阻塞。
        messageHandler.setDefaultTopic(defaultTopic);
        return messageHandler;
    }

    /**
     * @MessagingGateway 是一个用于提供消息网关代理整合的注解，参数defaultRequestChannel指定发送消息绑定的channel。
     * 在这里我们定义了MqttGateway接口，该接口可以被注入到其它类中，用于消息发送。
     */
    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MqttGateway {
        // 定义重载方法，用于消息发送
        void sendToMqtt(String payload);
        // 指定topic进行消息发送
        void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, String payload);
        /**
         * 发送信息到MQTT服务器
         *
         * @param topic 主题
         * @param qos 对消息处理的几种机制。<br> 0 表示的是订阅者没收到消息不会再次发送，消息会丢失。<br>
         * 1 表示的是会尝试重试，一直到接收到消息，但这种情况可能导致订阅者收到多次重复消息。<br>
         * 2 多了一次去重的动作，确保订阅者收到的消息有一次。
         * @param payload 消息主体
         */
        void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.QOS) int qos, String payload);
    }


}
