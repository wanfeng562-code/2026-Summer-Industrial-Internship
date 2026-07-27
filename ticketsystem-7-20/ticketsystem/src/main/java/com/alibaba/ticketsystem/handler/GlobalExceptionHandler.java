package com.alibaba.ticketsystem.handler;

import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.utils.R;
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
}
