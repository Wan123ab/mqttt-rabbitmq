package com.wq.mqtttrabbitmq.rabbitmq.config;


import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.DefaultExceptionHandler;
import com.wq.mqtttrabbitmq.rabbitmq.bean.MailMessageModel;
import com.wq.mqtttrabbitmq.rabbitmq.listener.MailMessageListenerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**RabbitMQ配置类
 * Spring AMQP 是对原生的 RabbitMQ 客户端的封装。一般情况下，我们只需要定义交换器的持久化和队列的持久化。
 *
 * 做到消息不能丢失，我们就要实现可靠消息，做到这一点，我们要做到下面二点：
 * 一：持久化
 * 1: exchange要持久化
 * 2: queue要持久化
 * 3: message要持久化（spring amqp默认已实现）
 * 二：消息确认
 * 1: 启动消费返回（@ReturnList注解，生产者就可以知道哪些消息没有发出去）
 * 2：生产者和Server（broker）之间的消息确认。
 * 3: 消费者和Server（broker）之间的消息确认。
 *
 */
@Configuration
@PropertySource(value = {"classpath:rabbitmq.properties"})
@Slf4j
public class RabbitMQConfig {
    @Autowired
    private Environment env;

    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(env.getProperty("mq.host").trim());
        connectionFactory.setPort(Integer.parseInt(env.getProperty("mq.port").trim()));
        connectionFactory.setVirtualHost(env.getProperty("mq.vhost").trim());
        connectionFactory.setUsername(env.getProperty("mq.username").trim());
        connectionFactory.setPassword(env.getProperty("mq.password").trim());
        /**
         * 设置异常处理器
         */
        connectionFactory.setExceptionHandler(new DefaultExceptionHandler(){
            @Override
            public void handleConfirmListenerException(Channel channel, Throwable e) {
                log.error("=====消息确认发生异常=======",e);
            }
        });
        return connectionFactory;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory(connectionFactory());
        /**
         * 为避免出现网络异常等原因造成的connection closed问题，此处设置缓存信道
         */
        factory.setChannelCacheSize(100);

        /**
         * 设置为True，当消息不能正确的路由到Queue时，将会返回给生产者。配合RabbitTemplate
         * 的setReturnCallback使用。
         *
         * 设置Returnlistener步骤
         * 1、设置factory.setPublisherReturns(true);
         * 2、rabbitTemplate.setMandatory(true)
         * 3、rabbitTemplate.setReturnCallback
         */
        factory.setPublisherReturns(true);

        /**
         * enable confirm mode
         * 设置为True，配合RabbitTemplate的setConfirmCallback使用。
         */
        factory.setPublisherConfirms(true);

        return factory;
    }

    /**
     * 监听器容器工厂类，使用@EnableRabbit和@RabbitListener必须先配置这个工厂类
     * springboot默认已经配置了，但是此处仍然进行显示配置以便实现个性化设置
     * @param connectionFactory
     * @return
     */
    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(cachingConnectionFactory());
        /**
         * 消费确认（comsumer acknowledgements）
         * broker与消费者之间的消息确认称为comsumer acknowledgements，comsumer acknowledgements
         * 机制用于解决消费者与Rabbitmq服务器之间消息可靠传输，它是在消费端消费成功之后通知broker消费端
         * 消费消息成功从而broker删除这个消息
         */
        //默认的确认模式是AcknowledgeMode.AUTO
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        //设置消费者监听器的消息转换器
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        /**
         * 告诉broker一次性最多往1个channel投递100条消息。通常将这个值设置大一点可以提高处理速度（默认250）
         * 1、这个参数只有手动确认才能生效。
         * 2、客户端可以利用这个参数来提高性能和做流量控制。如果prefetch设置的是10,
         * 当这个Channel上unacked的消息数量到达10条时，RabbitMq便不会在向这个channel发送消息
         * 3、如果有事务的话，必须大于等于transaction数量。
         * 4、可在Web控制台中channels选项卡进行查看
         * 5、也可以在@RabbitListener上设置
         */
//        factory.setPrefetchCount(100);

        /**
         * 设置每一个Queue的并行消费者。这里设置10个相当于1个Queue有10个channel对其进行监听消费
         * 相当于为某个Queue配置10个listener（1个listener默认启用1个线程，而1个线程只会打开1个channel）。
         * 当设置为1的时候表示只有1个消费者，适用于需要严格按照先进先出规则的Queue
         * 也可以在@RabbitListener上设置
         */
//        factory.setConcurrentConsumers(5);
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() throws Exception {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory());

        //TODO 这个事务怎么用的，似乎没起作用？？？  注意：确认confirms模式与实务tx互斥，：一个具有事务的channel不能放入到确认模式，同样确认模式下的channel不能用事务
//        rabbitTemplate.setChannelTransacted(true);
        /**
         * 如果mandatory有设置，则当消息不能路由到队列中去的时候，会触发return method。
         * 如果mandatory没有设置，则当消息不能路由到队列的时候，server会删除该消息
         */
        rabbitTemplate.setMandatory(true);

        /**
         * 设置生产者发送消息消息转换器
         */
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());

        /**
         * 发送确认（publisher acknowledgements）：消息发送到交换器成功
         * 生产者与broker之间的消息确认称为public confirms，public confirms机制用于解决生产者与
         * Rabbitmq服务器之间消息可靠传输，它在消息发送到交换器成功后通知消息生产者发送成功。
         */
        rabbitTemplate.setConfirmCallback((correlationData,ack,cause) -> {

            if(ack){
                System.out.println("消息id为: 【"+correlationData+"】的消息，发送到交换器成功");
            }else{
                //TODO
                // 发送端可以维护一个消息表（MYSQL），correlationData记录消息ID，当broker持久化失败时再次进行发送，
                // 当然这样不可避免的就会在消息端可能会造成消息的重复消息。针对消费端重复消息，在消费端进行幂等处理
                log.error("========【消息发送到交换器失败】,id为: 【"+correlationData+"】的消息，发送到交换器失败，失败原因是："+cause);
            }
        });

        /**
         * 设置消息路由到Queue失败回调
         */
        rabbitTemplate.setReturnCallback((message,replyCode,replyText,exchange,routingKey) -> {

            String messageBody = new String(message.getBody());
            MailMessageModel model = JSONObject.toJavaObject(JSONObject.parseObject(messageBody), MailMessageModel.class);
            log.error("============【消息路由到Queue失败】，id={},replyCode={},replyText={}," +
                    "exchange={},routingKey={}",model.getId(),replyCode,replyText,exchange,routingKey);
        });
        return rabbitTemplate;
    }

    @Bean
    public RabbitAdmin amqpAdmin() throws Exception {
        RabbitAdmin rabbitAdmin =  new RabbitAdmin(cachingConnectionFactory());
        /**
         * 忽略声明式异常
         */
        rabbitAdmin.setIgnoreDeclarationExceptions(true);
        return new RabbitAdmin(cachingConnectionFactory());
    }

    @Bean
    public Queue queue() {
        String name = env.getProperty("mq.queue").trim();
        // 是否持久化
        boolean durable = StringUtils.isNotBlank(env.getProperty("mq.queue.durable").trim())?
                Boolean.valueOf(env.getProperty("mq.queue.durable").trim()) : true;
        // 仅创建者可以使用的私有队列，断开后自动删除
        boolean exclusive = StringUtils.isNotBlank(env.getProperty("mq.queue.exclusive").trim())?
                Boolean.valueOf(env.getProperty("mq.queue.exclusive").trim()) : false;
        // 当所有消费客户端连接断开后，是否自动删除队列
        boolean autoDelete = StringUtils.isNotBlank(env.getProperty("mq.queue.autoDelete").trim())?
                Boolean.valueOf(env.getProperty("mq.queue.autoDelete").trim()) : false;
        return new Queue(name, durable, exclusive, autoDelete);
    }

    /**
     * TTL延时队列
     * @return
     */
    @Bean
    public Queue ttlqueue() {
        String name = env.getProperty("mq.ttlqueue").trim();
        // 是否持久化
        boolean durable = StringUtils.isNotBlank(env.getProperty("mq.ttlqueue.durable").trim())?
                Boolean.valueOf(env.getProperty("mq.ttlqueue.durable").trim()) : true;
        // 仅创建者可以使用的私有队列，断开后自动删除
        boolean exclusive = StringUtils.isNotBlank(env.getProperty("mq.ttlqueue.exclusive").trim())?
                Boolean.valueOf(env.getProperty("mq.ttlqueue.exclusive").trim()) : false;
        // 当所有消费客户端连接断开后，是否自动删除队列
        boolean autoDelete = StringUtils.isNotBlank(env.getProperty("mq.ttlqueue.autoDelete").trim())?
                Boolean.valueOf(env.getProperty("mq.ttlqueue.autoDelete").trim()) : false;

        Map<String, Object> arguments = new HashMap<>();
        /**
         * 设置队列消息生存时间
         */
        arguments.put("x-message-ttl",Integer.valueOf(env.getProperty("mq.ttlqueue.time")));
        /**
         * 设置队列的死信交换器。
         * 通常TTL+DLX一起使用实现延时任务
         */
        arguments.put("x-dead-letter-exchange",String.valueOf(env.getProperty("mq.dead_exchange").trim()));
        /**
         * 设置死信路由Key，若不设置默认使用当前Queue（ttlqueue）绑定的Key。
         * 当出现实心消息时将按照此处指定的交换器和路由规则进入对应的死信队列deadqueue。
         */
        arguments.put("x-dead-letter-routing-key",String.valueOf(env.getProperty("mq.deadroutekey").trim()));

        return new Queue(name, durable, exclusive, autoDelete,arguments);
    }

    @Bean
    public Queue priorityqueue() {
        String name = env.getProperty("mq.priorityqueue").trim();
        // 是否持久化
        boolean durable = StringUtils.isNotBlank(env.getProperty("mq.priorityqueue.durable").trim())?
                Boolean.valueOf(env.getProperty("mq.priorityqueue.durable").trim()) : true;
        // 仅创建者可以使用的私有队列，断开后自动删除
        boolean exclusive = StringUtils.isNotBlank(env.getProperty("mq.priorityqueue.exclusive").trim())?
                Boolean.valueOf(env.getProperty("mq.priorityqueue.exclusive").trim()) : false;
        // 当所有消费客户端连接断开后，是否自动删除队列
        boolean autoDelete = StringUtils.isNotBlank(env.getProperty("mq.priorityqueue.autoDelete").trim())?
                Boolean.valueOf(env.getProperty("mq.priorityqueue.autoDelete").trim()) : false;

        Map<String, Object> arguments = new HashMap<>();
        /**
         * 设置队列优先级别，有10个优先级别
         */
        arguments.put("x-max-priority",Integer.valueOf(env.getProperty("mq.priorityqueue.level")));

        /**
         * 设置队列的死信交换器。
         * 通常TTL+DLX一起使用实现延时任务
         */
        arguments.put("x-dead-letter-exchange",String.valueOf(env.getProperty("mq.dead_exchange").trim()));
        /**
         * 设置死信路由Key，若不设置默认使用当前Queue（ttlqueue）绑定的Key。
         * 当出现实心消息时将按照此处指定的交换器和路由规则进入对应的死信队列deadqueue。
         */
        arguments.put("x-dead-letter-routing-key",String.valueOf(env.getProperty("mq.deadroutekey").trim()));
        return new Queue(name, durable, exclusive, autoDelete,arguments);
    }

    @Bean
    public TopicExchange exchange() {
        String name = env.getProperty("mq.exchange").trim();
        // 是否持久化
        boolean durable = StringUtils.isNotBlank(env.getProperty("mq.exchange.durable").trim())?
                Boolean.valueOf(env.getProperty("mq.exchange.durable").trim()) : true;
        // 当所有消费客户端连接断开后，是否自动删除队列
        boolean autoDelete = StringUtils.isNotBlank(env.getProperty("mq.exchange.autoDelete").trim())?
                Boolean.valueOf(env.getProperty("mq.exchange.autoDelete").trim()) : false;
        return new TopicExchange(name, durable, autoDelete);
    }

    /**
     * 用routekey绑定TopicExchange和Queue
     * 这个地方如果绑定错误导致消息路由到Queue失败将不会触发ReturnCallback
     * @return
     */
    @Bean
    public Binding binding() {
        String routekey = env.getProperty("mq.routekey").trim();
        return BindingBuilder.bind(queue()).to(exchange()).with(routekey);
    }

    @Bean
    public Binding prioritybinding() {
        String routekey = env.getProperty("mq.priorityroutekey").trim();
        return BindingBuilder.bind(priorityqueue()).to(exchange()).with(routekey);
    }

    @Bean
    public Binding ttlbinding() {
        String ttlroutekey = env.getProperty("mq.ttlroutekey").trim();
        return BindingBuilder.bind(ttlqueue()).to(exchange()).with(ttlroutekey);
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue deadqueue() {
        String name = env.getProperty("mq.deadqueue").trim();
        // 是否持久化
        boolean durable = StringUtils.isNotBlank(env.getProperty("mq.deadqueue.durable").trim())?
                Boolean.valueOf(env.getProperty("mq.deadqueue.durable").trim()) : true;
        // 仅创建者可以使用的私有队列，断开后自动删除
        boolean exclusive = StringUtils.isNotBlank(env.getProperty("mq.deadqueue.exclusive").trim())?
                Boolean.valueOf(env.getProperty("mq.deadqueue.exclusive").trim()) : false;
        // 当所有消费客户端连接断开后，是否自动删除队列
        boolean autoDelete = StringUtils.isNotBlank(env.getProperty("mq.deadqueue.autoDelete").trim())?
                Boolean.valueOf(env.getProperty("mq.deadqueue.autoDelete").trim()) : false;

        return new Queue(name, durable, exclusive, autoDelete);
    }

    /**
     * 死信交换器
     * 在某个队列上指定一个Exchange，则在该队列上发生如下情况，
     * 1.消息被拒绝（basic.reject or basic.nack)，且requeue=false
     * 2.消息过期而被删除（TTL）
     * 3.消息数量超过队列最大限制而被删除
     * 4.消息总大小超过队列最大限制而被删除
     * 就会把该消息转发到指定的这个exchange
     * @return
     */
    @Bean
    public DirectExchange deadexchange() {
        String name = env.getProperty("mq.dead_exchange").trim();
        // 是否持久化
        boolean durable = StringUtils.isNotBlank(env.getProperty("mq.dead_exchange.durable").trim())?
                Boolean.valueOf(env.getProperty("mq.dead_exchange.durable").trim()) : true;
        // 当所有消费客户端连接断开后，是否自动删除队列
        boolean autoDelete = StringUtils.isNotBlank(env.getProperty("mq.dead_exchange.autoDelete").trim())?
                Boolean.valueOf(env.getProperty("mq.dead_exchange.autoDelete").trim()) : false;
        return new DirectExchange(name, durable, autoDelete);
    }

    /**
     * 绑定死信队列和死信路由
     * @return
     */
    @Bean
    public Binding deadbinding() {
        String deadroutekey = env.getProperty("mq.deadroutekey").trim();
        return BindingBuilder.bind(deadqueue()).to(deadexchange()).with(deadroutekey);
    }

    /**
     * 日志队列
     * @return
     */
    @Bean
    public Queue logqueue() {
        String name = env.getProperty("mq.logqueue").trim();
        // 是否持久化
        boolean durable = StringUtils.isNotBlank(env.getProperty("mq.logqueue.durable").trim())?
                Boolean.valueOf(env.getProperty("mq.logqueue.durable").trim()) : true;
        // 仅创建者可以使用的私有队列，断开后自动删除
        boolean exclusive = StringUtils.isNotBlank(env.getProperty("mq.logqueue.exclusive").trim())?
                Boolean.valueOf(env.getProperty("mq.logqueue.exclusive").trim()) : false;
        // 当所有消费客户端连接断开后，是否自动删除队列
        boolean autoDelete = StringUtils.isNotBlank(env.getProperty("mq.logqueue.autoDelete").trim())?
                Boolean.valueOf(env.getProperty("mq.logqueue.autoDelete").trim()) : false;

        return new Queue(name, durable, exclusive, autoDelete);
    }

    /**
     * 日志交换机
     * @return
     */
    @Bean
    public DirectExchange logexchange() {
        String name = env.getProperty("mq.logexchange").trim();
        // 是否持久化
        boolean durable = StringUtils.isNotBlank(env.getProperty("mq.logexchange.durable").trim())?
                Boolean.valueOf(env.getProperty("mq.logexchange.durable").trim()) : true;
        // 当所有消费客户端连接断开后，是否自动删除队列
        boolean autoDelete = StringUtils.isNotBlank(env.getProperty("mq.logexchange.autoDelete").trim())?
                Boolean.valueOf(env.getProperty("mq.logexchange.autoDelete").trim()) : false;
        return new DirectExchange(name, durable, autoDelete);
    }

    /**
     * 绑定日志队列和日志路由
     * @return
     */
    @Bean
    public Binding logbinding() {
        String routekey = env.getProperty("mq.logroutekey").trim();
        return BindingBuilder.bind(logqueue()).to(logexchange()).with(routekey);
    }

    /**
     * 配置启用rabbitmq事务。注意：确认并且保证消息被送达，提供了两种方式：发布确认和事务。
     * (两者不可同时使用)在channel为事务时，不可引入确认模式；同样channel为确认模式下，不可使用事务
     * @return
     */
//    @Bean
//    public RabbitTransactionManager rabbitTransactionManager() {
//        return new RabbitTransactionManager(cachingConnectionFactory());
//    }

    //下面配置等同于@RabbitListener
//    @Bean
//    public SimpleMessageListenerContainer listenerContainer(
//            @Qualifier("mailMessageListenerAdapter") MailMessageListenerAdapter mailMessageListenerAdapter) throws Exception {
//        String queueName = env.getProperty("mq.queue").trim();
//
//        SimpleMessageListenerContainer simpleMessageListenerContainer =
//                new SimpleMessageListenerContainer(cachingConnectionFactory());
//        simpleMessageListenerContainer.setQueueNames(queueName);
//        simpleMessageListenerContainer.setMessageListener(mailMessageListenerAdapter);
//        /**
//         *  设置手动 ACK
//         */
//        simpleMessageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
//        return simpleMessageListenerContainer;
//    }

}
