package com.wq.mqtttrabbitmq.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseResult<T> implements Serializable {

    private int code;

    private String msg;

    private T data;

}
