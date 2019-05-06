package com.wq.mqtttrabbitmq.common.log;

import com.alibaba.fastjson.JSONObject;
import com.wq.mqtttrabbitmq.es.EsClient;
import com.wq.mqtttrabbitmq.rabbitmq.client.RabbitClient;
import com.wq.mqtttrabbitmq.utils.IPUtils;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Plugin(name = "ESAppender", category = "Core", elementType = "appender", printObject = true)
public class ESAppender extends AbstractAppender {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static ExecutorService executorService = Executors.newFixedThreadPool(5);

    /*写入ES的索引*/
    private static String esIndex;

    /*写入ES索引中的type*/
    private static String esType;

    protected ESAppender(String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
    }

    @Override
    public void append(LogEvent event) {

        JSONObject jsonObject = new JSONObject();
        ThrowableProxy thrownProxy = event.getThrownProxy();

        jsonObject.put("time", sdf.format(new Date()));
        jsonObject.put("className", event.getLoggerName());
        jsonObject.put("methodName", event.getSource().getMethodName());
        jsonObject.put("logMessage", event.getMessage().getFormattedMessage());

        jsonObject.put("ip", IPUtils.getIp());
        jsonObject.put("logLevel", event.getLevel().name());
        jsonObject.put("logThread", event.getThreadName());

        jsonObject.put("errorMsg", thrownProxy == null ? "" : thrownProxy.getMessage());
        jsonObject.put("exception", thrownProxy == null ? "" : thrownProxy.getName());
        jsonObject.put("stackTrace", thrownProxy == null ? "" : thrownProxy.getExtendedStackTraceAsString());

        executorService.execute(() -> {
            try {
                /*写入ES*/
//                EsClient.addDataBulk(Collections.singletonList(jsonObject), esIndex, esType);

                /*发送到RabbitMQ*/
                RabbitClient.send("log_exchange", "log_routekey", jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 接收配置文件中的参数
     *
     * @param name
     * @param filter
     * @param layout
     * @return
     */
    @PluginFactory
    public static ESAppender createAppender(@PluginAttribute("name") String name,
                                            @PluginElement("Filter") final Filter filter,
                                            @PluginElement("Layout") Layout<? extends Serializable> layout,
                                            @PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
                                            @PluginAttribute("index") String index,
                                            @PluginAttribute("type") String type,
                                            @PluginElement("properties") Property[] properties) {
        if (name == null) {
            LOGGER.error("No name provided for MyCustomAppenderImpl");
            return null;
        }
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }

        esIndex = index;
        esType = type;

        return new ESAppender(name, filter, layout, ignoreExceptions, properties);
    }


}
