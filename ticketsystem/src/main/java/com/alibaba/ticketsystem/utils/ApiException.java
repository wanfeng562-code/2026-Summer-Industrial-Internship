package com.alibaba.ticketsystem.utils;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * RuntimeException  运行时异常
 */
@Getter
public class ApiException extends RuntimeException{

    private final HttpStatus status;
    private final Object data;

    public ApiException(String message) {
        this(HttpStatus.BAD_REQUEST, message, null);
    }

    public ApiException(HttpStatus status, String message) {
        this(status, message, null);
    }

    public ApiException(HttpStatus status, String message, Object data) {
        super(message);
        this.status = status;
        this.data = data;
    }
}
