package com.wq.mqtttrabbitmq.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author 万强
 * @date 2019/5/28 09:27
 * @desc ThreadLocal工具类，实现线程级别全局缓存
 */
public class ThreadLocalUtils {

    private static final ThreadLocal<Map<String,Object>> cache = new ThreadLocal<>();

    private static final Predicate predicate = t -> t == null;

    public static void set(String key,Object value){
        if(predicate.test(cache.get())){
            reset();
        }
        cache.get().put(key,value);
    }

    public static <T> T get(String key){
        return (T) Optional.ofNullable(cache.get()).map(m -> m.get(key)).orElse(null);
    }

    public static void remove(String key){
        if(predicate.test(cache.get())){
            return;
        }
        cache.get().remove(key);
    }

    public static void clear(){
        cache.remove();
    }

    private static void reset(){
        cache.set(new HashMap<>());
    }



    public static void main(String[] args) {
        set("a","张三");
        Object a = get("a");
        System.out.println(a);
    }


}
