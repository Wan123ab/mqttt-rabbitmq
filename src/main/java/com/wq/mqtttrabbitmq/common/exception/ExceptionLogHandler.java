package com.wq.mqtttrabbitmq.common.exception;

import com.wq.mqtttrabbitmq.mqtt.client.MyMqttClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 异常日志全局处理器（只能捕获controller层异常）
 * 1、局部异常和全局异常处理同一种类型的 Exception，会发生什么结果？
 * 答：只会执行局部异常的处理逻辑！
 * 2、GirlFriendNotFoundException 继承了 RuntimeException， 使用
 * @ExceptionHandler(RuntimeException.class) 能处理异常吗？
 * 答：可以的！所以对于局部比较公用的异常可以定义一个父类，抛出异常时可以抛出具体的子类异常，处理时，处理父类异常即可（即只用写一个方法处理一系列类似的异常）
 *
 */
@ControllerAdvice
@Slf4j
@AllArgsConstructor
public class ExceptionLogHandler {

    @Autowired
    private MyMqttClient mqttClient;

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult handleException(Exception e){
        log.error("出异常啦，errorMsg={}",e.getMessage(), e);
//        printExceptionn(e);
        mqttClient.publish("errorLog",ExceptionUtils.getStackTrace(e));
        return new ResponseResult(500,ExceptionUtils.getMessage(e),null);
    }


    private void printExceptionn(Exception e) {
        System.out.println(ExceptionUtils.getMessage(e));
        System.out.println(ExceptionUtils.getStackTrace(e));
        System.out.println(ExceptionUtils.getRootCause(e));
        System.out.println(ExceptionUtils.getRootCauseMessage(e));
    }

}
