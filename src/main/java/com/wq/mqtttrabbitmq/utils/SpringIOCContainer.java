package com.wq.mqtttrabbitmq.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringIOCContainer implements ApplicationContextAware {

    /**
     * spring无法直接给静态变量注入值，因为静态变量不属于对象，只属于类，也就是说在类被加载字节码的时候变量已经初始化了，也就是给该变量分配内存了，导致spring忽略静态变量。
     * 所以@Autowired无法注入下面的静态变量。
     * 解决方法：
     * 实现ApplicationContextAware接口，然后在static{}中通过SpringIOCContainer获取，详见AA测试类
     */

    private static ApplicationContext ac;

    public static <T> T getBean(String name, Class<T> requiredType){

        return ac.getBean(name,requiredType);

    }

    public static <T> T getBean(Class<T> requiredType){

        return ac.getBean(requiredType);

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ac = applicationContext;
    }

}
