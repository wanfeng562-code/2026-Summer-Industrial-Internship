package com.alibaba.ticketsystem.utils;

import lombok.Data;

@Data
public class R<T> {

    private Integer code;  //    200表示成功，500表示失败  500 服务器异常  404 找不到

    private String msg;  // 返回正确的或错误的信息

    private  T data;  // 返回的数据

    //私有的全参构造函数
    private R(Integer code, String msg,T data){
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    //返回正确的消息
    public static <T> R<T> success(String msg) { //静态方法，直接调用
        return new R<T>(200, msg, null);
    }

    //返回正确的结果集
    public static <T> R<T> success(String msg, T data) { //静态方法，直接调用
        return new R<T>(200, msg, data);
    }

    //返回错误的消息
    public static <T> R<T> failure(String msg) { //静态方法，直接调用
        return new R<T>(500, msg, null);
    }

    //返回自定义的消息
    public static <T> R<T> failure(Integer code, String msg) { //静态方法，直接调用
        return new R<T>(code, msg, null);
    }

    public static <T> R<T> failure(Integer code, String msg, T data) { //静态方法，直接调用
        return new R<T>(code, msg, data);
    }
}
