package com.wq.mqtttrabbitmq.common.MDC;

import com.wq.mqtttrabbitmq.utils.IDUtils;
import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 日志拦截器，用于拦截所有请求，并在请求中添加sessionId以便实现日志链路跟踪
 */
public class LogInterceptor extends HandlerInterceptorAdapter {

    private static final String SESSION_KEY = "sessionId";
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /**
         * 设置sessionId
         */
        MDC.put(SESSION_KEY,IDUtils.getId());
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        /**
         * 请求结束，移除sessionId
         */
        MDC.remove(SESSION_KEY);
        super.afterCompletion(request, response, handler, ex);
    }
}
