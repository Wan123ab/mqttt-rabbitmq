package com.wq.mqtttrabbitmq.common.filter;

import com.wq.mqtttrabbitmq.utils.IPUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * MDC全局拦截器，用于记录访问日志（用户、IP地址、Method、URI、请求参数、请求体）
 */
@Component
@Slf4j
public class MDCFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        try {
            MDC.put("user", request.getRemoteUser());
            String query = request.getQueryString() != null ? "?" + request.getQueryString() : "";
            if (request.getMethod().equals(HttpMethod.POST.name())) {
                MultiReadHttpServletRequest multiReadHttpServletRequest = new MultiReadHttpServletRequest(request);
                log.info("IP:{}, Method:{}, URI:{} Body:{}", IPUtils.getClientIP(request), request.getMethod(), request.getRequestURI() + query, multiReadHttpServletRequest.getRequestBody());
                chain.doFilter(multiReadHttpServletRequest, response);
            } else {
                log.info("IP:{}, Method:{}, URI:{}", IPUtils.getClientIP(request), request.getMethod(), request.getRequestURI() + query);
                chain.doFilter(request, response);
            }
        } finally {
            MDC.clear();
        }
    }

    /**
     * HttpServletRequest 请求体多读
     */
    class MultiReadHttpServletRequest extends HttpServletRequestWrapper {

        // 缓存 RequestBody
        private String requestBody;

        MultiReadHttpServletRequest(HttpServletRequest request) {
            super(request);
            requestBody = "";
            try {
                StringBuilder stringBuilder = new StringBuilder();
                InputStream inputStream = request.getInputStream();
                byte[] bs = new byte[1024];
                int len;
                while ((len = inputStream.read(bs)) != -1) {
                    stringBuilder.append(new String(bs, 0, len));
                }
                requestBody = stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(requestBody.getBytes());

            return new ServletInputStream() {
                public int read() throws IOException {
                    return byteArrayInputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return byteArrayInputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {

                }
            };
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(this.getInputStream()));
        }

        String getRequestBody() {
            return requestBody.replaceAll("\n", "");
        }
    }

}
