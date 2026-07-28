package com.alibaba.ticketsystem.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.utils.R;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理：统一返回 code / msg / data
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<R<?>> exceptionHandler(ApiException e) {
        int code = e.getCode();
        HttpStatus status = code == 401 ? HttpStatus.UNAUTHORIZED
                : code == 403 ? HttpStatus.FORBIDDEN
                : code == 404 ? HttpStatus.NOT_FOUND
                : code >= 500 ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(R.failure(code, e.getMessage()), status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<?>> methodException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMsg = error.getDefaultMessage();
            errors.put(fieldName, errorMsg);
        });
        return new ResponseEntity<>(R.failure(400, "参数校验异常", errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<?>> handleNotLoginException(NotLoginException e) {
        return new ResponseEntity<>(R.failure(401, "未登录或登录已失效"), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<R<?>> handleNotRoleException(NotRoleException e) {
        return new ResponseEntity<>(R.failure(403, "无此角色权限，禁止访问"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<R<?>> handleNotPermissionException(NotPermissionException e) {
        return new ResponseEntity<>(R.failure(403, "权限不足，禁止访问"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<?>> handleException(Exception e) {
        return new ResponseEntity<>(R.failure(500, "服务器内部错误"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
