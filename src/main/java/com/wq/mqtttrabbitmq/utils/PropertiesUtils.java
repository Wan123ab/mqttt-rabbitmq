package com.wq.mqtttrabbitmq.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtils {

    private static final Logger log = LoggerFactory.getLogger(PropertiesUtils.class);

    /**
     * 私有构造函数，禁止在外部创建该类的实例
     */
    private PropertiesUtils() {
    }

    private static class SingleHolder {
        private static PropertiesUtils instance = new PropertiesUtils();
    }

    public static PropertiesUtils getInstance() {
        return SingleHolder.instance;
    }

    public String getProperty(String fileName, String propertyName) {
        Properties prop = new Properties();
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(fileName);
            prop.load(is);
            if (is != null)
                is.close();
        } catch (Exception e) {
            log.error(e + "file " + fileName + " not found");
            e.printStackTrace();
        }
        return prop.getProperty(propertyName);
    }

    public String appendSlash(String url) {
        String rst = url.trim().replaceAll("\\\\", "/");

        if (rst.endsWith("/")) {
            return rst;
        } else {
            return rst + "/";
        }
    }
}
