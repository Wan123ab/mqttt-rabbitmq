package com.wq.mqtttrabbitmq.rabbitmq.listener;

import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.wq.mqtttrabbitmq.rabbitmq.bean.MailMessageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;

/**rabbitmq消息监听适配器，当前适配器用于监听消息然后发送邮件
 * Spring 对 Java Mail 有很好的支持。其中，邮件包括几种类型：
 * 简单文本的邮件、 HTML 文本的邮件、 内嵌图片的邮件、 包含附件的邮件。这里，我们封装了一个简单的 sendHtmlMail() 进行邮件发送。
 */
@Component
@Slf4j
public class MailMessageListenerAdapter extends MessageListenerAdapter {

    @Resource
    /*spring对java email的支持*/
    private JavaMailSender mailSender;

    @Value("${mail.username}")
    private String mailUsername;

    /**
     * 在 onMessage() 方法中，我们完成了三件事情：
     * 1. 从 RabbitMQ 的消息队列中解析消息体。
     * 2. 根据消息体的内容，发送邮件给目标的邮箱。
     * 3. 手动应答 ACK，让消息队列删除该消息。
     * @param message
     * @param channel
     * @throws Exception
     */
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        String messageBody = null;
        try {
            // 解析RabbitMQ消息体
            messageBody = new String(message.getBody());
            MailMessageModel mailMessageModel = JSONObject.toJavaObject(JSONObject.parseObject(messageBody), MailMessageModel.class);
            // 发送邮件
            String to =  mailMessageModel.getTo();
            String subject = mailMessageModel.getSubject();
            String text = mailMessageModel.getText();
            sendHtmlMail(to, subject, text);
            // 手动ACK
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            //模拟消息异常
            //System.out.println(1/0);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            log.error("MailMessageListenerAdapter消费消息失败，message={}",messageBody);
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
