package com.wq.mqtttrabbitmq.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T json2Obj(String json, Class<T> clz) {

        try {
            return mapper.readValue(json, clz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String obj2Json(Object o) {

        try {
            return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static byte[] obj2ByteArr(Object o) {

        try {
            return mapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }


}
