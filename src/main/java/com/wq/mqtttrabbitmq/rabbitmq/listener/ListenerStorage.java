package com.wq.mqtttrabbitmq.rabbitmq.listener;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.wq.mqtttrabbitmq.rabbitmq.bean.MailMessageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Header;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;

/**监听器仓库类，可使用@EnableRabbit和@RabbitListener注册多个监听器，效果等同于
 * SimpleMessageListenerContainer。
 *
 * 使用spring-amqp的@RabbitListener注解的时候，必须声明org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory对象，
 * 而如果使用springboot的方式则不需要自己在容器中声明org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory
 *
 */
@Configuration
@EnableRabbit
@Slf4j
public class ListenerStorage {

    @Resource
    /*spring对java email的支持*/
    private JavaMailSender mailSender;

    @Value("${mail.username}")
    private String mailUsername;

    @RabbitListener(queues = "${mq.queue}")
    public void onMessage1(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        String messageBody = null;
        MailMessageModel mailMessageModel = null;
        try {
            // 解析RabbitMQ消息体
            messageBody = new String(message.getBody());
            mailMessageModel = JSONObject.toJavaObject(JSONObject.parseObject(messageBody), MailMessageModel.class);
            // 发送邮件
            String to =  mailMessageModel.getTo();
            String subject = mailMessageModel.getSubject();
            String text = mailMessageModel.getText();

//            发送邮件
//            sendHtmlMail(to, subject, text);

            //此处异常将导致消息消费失败，在catch中重新入列，broker会投递给其他消费者
            System.out.println(1/0);

            // 手动ACK
//          channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            //上面代码也可以
            channel.basicAck(tag, false);

            //此处异常将导致消息被消费，无法重新入列，因为上面的channel.basicAck(tag, false);已经确认消费了
//            System.out.println(1/0);

            log.info("onMessage1消费消息成功，id={},message={}",mailMessageModel.getId(),messageBody);
        }catch (Exception e){
            //拒绝消息，此消息将重新入列到broker
            channel.basicReject(tag,true);
            log.error("onMessage1消费消息失败，id={},message={}",mailMessageModel.getId(),messageBody,e);
            throw e;
        }
    }

    @RabbitListener(queues = "${mq.queue}")
    public void onMessage2(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        String messageBody = null;
        MailMessageModel mailMessageModel = null;
        try {
            // 解析RabbitMQ消息体
            messageBody = new String(message.getBody());
            mailMessageModel = JSONObject.toJavaObject(JSONObject.parseObject(messageBody), MailMessageModel.class);
            // 发送邮件
            String to =  mailMessageModel.getTo();
            String subject = mailMessageModel.getSubject();
            String text = mailMessageModel.getText();

//            发送邮件
//            sendHtmlMail(to, subject, text);
            // 手动ACK
//          channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            //上面代码也可以
            channel.basicAck(tag, false);

            //此处异常将导致消费失败！消息重新入列
//            System.out.println(1/0);

            log.info("onMessage2消费消息成功，id={},message={}",mailMessageModel.getId(),messageBody);
        }catch (Exception e){
            //拒绝消息，此消息将重新入列到broker
            channel.basicReject(tag,true);
            log.error("onMessage2消费消息失败，id={},message={}",mailMessageModel.getId(),messageBody,e);

        }
    }

    @RabbitListener(queues = "${mq.queue}")
    public void onMessage3(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        String messageBody = null;
        MailMessageModel mailMessageModel = null;
        try {
            // 解析RabbitMQ消息体
            messageBody = new String(message.getBody());
            mailMessageModel = JSONObject.toJavaObject(JSONObject.parseObject(messageBody), MailMessageModel.class);
            // 发送邮件
            String to =  mailMessageModel.getTo();
            String subject = mailMessageModel.getSubject();
            String text = mailMessageModel.getText();

//            发送邮件
//            sendHtmlMail(to, subject, text);

            //手动ACK
            channel.basicAck(tag, false);

            //此处异常将导致消费失败！消息重新入列
//            System.out.println(1/0);

            log.info("onMessage3消费消息成功，id={},message={}",mailMessageModel.getId(),messageBody);
        }catch (Exception e){
            //拒绝消息，此消息将重新入列到broker
            channel.basicReject(tag,true);
            log.error("onMessage3消费消息失败，id={},message={}",mailMessageModel.getId(),messageBody,e);

        }
    }

    /**
     * 监听延时队列
     * @param message
     * @param channel
     * @param tag
     * @throws Exception
     */
//    @RabbitListener(queues = "${mq.ttlqueue}")
    public void onMessageTTL(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws Exception {
        String messageBody = null;
        MailMessageModel mailMessageModel = null;
        try {
            // 解析RabbitMQ消息体
            messageBody = new String(message.getBody());
            mailMessageModel = JSONObject.toJavaObject(JSONObject.parseObject(messageBody), MailMessageModel.class);
            // 发送邮件
            String to =  mailMessageModel.getTo();
            String subject = mailMessageModel.getSubject();
            String text = mailMessageModel.getText();

            //手动ACK
            channel.basicAck(tag, false);


            log.info("onMessageTTL消费消息成功，id={},message={}",mailMessageModel.getId(),messageBody);
        }catch (Exception e){
            //拒绝消息，此消息将重新入列到broker
            channel.basicReject(tag,true);
            log.error("onMessageTTL消费消息失败，id={},message={}",mailMessageModel.getId(),messageBody,e);

        }
    }

    /**
     * 发送邮件
     * @param to
     * @param subject
     * @param text
     * @throws Exception
     */
    private void sendHtmlMail(String to, String subject, String text) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom(mailUsername);
        mimeMessageHelper.setTo(to);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(text, true);
        // 发送邮件
        mailSender.send(mimeMessage);
    }

}
