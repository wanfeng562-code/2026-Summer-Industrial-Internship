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
 * 全局异常处理类
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)  //捕获APIException异常
    public R<?> exceptionHandler(ApiException e){
        return R.failure(e.getMessage());
    }

    //捕获参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<?> methodException(MethodArgumentNotValidException e){
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error)->{
            String feildName = ((FieldError)error).getField();  //校验属性
            String errorMsg = error.getDefaultMessage();
            errors.put(feildName,errorMsg);
        });
        return R.failure(401,"参数校验异常", errors);
    }

    // 处理未登录异常
    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity handleNotLoginException(NotLoginException e) {
        //return R.error(401, "未登录或Token过期：" + e.getMessage(), null);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 401);
        response.put("message", "无此角色权限，禁止访问");
        response.put("data", null);

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // 处理无角色异常
    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity handleNotRoleException(NotRoleException e) {

        //return R.error(403, "无此角色权限：" + e.getMessage(), null);
        Map<String, Object> response = new HashMap<>();
        response.put("code", 403);
        response.put("message", "无此角色权限，禁止访问");
        response.put("data", null);

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // 处理无权限异常
    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity handleNotPermissionException(NotPermissionException e) {
        //return R.error(403, "无此操作权限：" + e.getMessage(), null);  //返回200状态码
        Map<String, Object> response = new HashMap<>();
        response.put("code", 403);
        response.put("message", "权限不足，禁止访问");
        response.put("data", null);

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN); //返回403状态码
    }
}
