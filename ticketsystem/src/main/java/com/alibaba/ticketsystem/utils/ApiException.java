package com.alibaba.ticketsystem.utils;

/**
 * RuntimeException  运行时异常
 */
public class ApiException extends RuntimeException{

    public ApiException(String message) {
        super(message);
    }
}
