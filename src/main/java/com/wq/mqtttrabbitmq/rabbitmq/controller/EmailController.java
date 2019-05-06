package com.wq.mqtttrabbitmq.rabbitmq.controller;

import com.wq.mqtttrabbitmq.rabbitmq.bean.MailMessageModel;
import com.wq.mqtttrabbitmq.rabbitmq.service.EmailService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Resource
    private EmailService emailService;

    private Executor executor = Executors.newCachedThreadPool();

    private int index = 0;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MailMessageModel add(@RequestBody MailMessageModel mailMessageModel) throws Exception {

        //调用最多30个线程发送30W条数据，测试
        for (int i = 0; i < 30; i++) {
            executor.execute(() -> {

                try {
                    for (int j = 0; j < 10000; j++) {
                        mailMessageModel.setId(index);
                        emailService.sendEmail(mailMessageModel);
                        incr();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return mailMessageModel;
    }

    /**
     * 测试ttl+dlx（死信队列）实现延时任务
     * @param mailMessageModel
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/ttl",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MailMessageModel addttl(@RequestBody MailMessageModel mailMessageModel) throws Exception {

        emailService.sendEmailTTL(mailMessageModel);

        return mailMessageModel;
    }

    @PostMapping(value = "/priority",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MailMessageModel addpriority(@RequestBody MailMessageModel mailMessageModel) throws Exception {

        emailService.sendEmailPriority(mailMessageModel);

        return mailMessageModel;
    }

    @PostMapping(value = "/priority/{num}",consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public MailMessageModel addpriority(@RequestBody MailMessageModel mailMessageModel,@PathVariable Integer num) throws Exception {

        emailService.sendEmailPriority(mailMessageModel,num);

        return mailMessageModel;
    }

    public synchronized void incr(){

        index ++ ;
        System.out.println("======================"+index+"================================");
    }
}
