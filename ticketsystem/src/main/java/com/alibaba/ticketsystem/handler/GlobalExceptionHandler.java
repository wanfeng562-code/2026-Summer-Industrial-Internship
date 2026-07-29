package com.alibaba.ticketsystem.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.utils.R;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<R<?>> handleApiException(ApiException e){
        HttpStatus status = e.getStatus();
        return ResponseEntity.status(status)
                .body(R.failure(status.value(), e.getMessage(), e.getData()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<R<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException e){
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError fieldError
                    ? fieldError.getField() : "_global";
            errors.put(fieldName, error.getDefaultMessage());
        });
        Map<String, Object> data = Map.of("fieldErrors", errors);
        return ResponseEntity.badRequest()
                .body(R.failure(400, "参数校验失败", data));
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<R<?>> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest()
                .body(R.failure(400, "请求参数格式不正确"));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<R<?>> handleDuplicateKey(DuplicateKeyException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(R.failure(409, "数据已存在，请勿重复提交"));
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<R<?>> handleNotLoginException(NotLoginException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(R.failure(401, "未登录或登录状态已失效"));
    }

    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<R<?>> handleNotRoleException(NotRoleException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(R.failure(403, "当前角色无权执行该操作"));
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<R<?>> handleNotPermissionException(NotPermissionException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(R.failure(403, "权限不足，禁止访问"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<?>> handleUnexpectedException(Exception e) {
        log.error("Unhandled server exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.failure(500, "服务器内部异常"));
    }
}
