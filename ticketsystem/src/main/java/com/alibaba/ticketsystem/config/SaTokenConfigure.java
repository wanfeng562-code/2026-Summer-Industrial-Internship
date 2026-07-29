package com.alibaba.ticketsystem.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 初次 REQUEST 已完成登录和注解权限校验。SSE 完成后的 ASYNC 二次派发不再重复
        // 访问已释放的 Sa-Token ThreadLocal 上下文。
        registry.addInterceptor(new AsyncAwareSaInterceptor())
                .addPathPatterns("/**") //对所有请进行拦截
                .excludePathPatterns("/user/login", "/user/register", "/error");
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.stream()
                .filter(StringHttpMessageConverter.class::isInstance)
                .map(StringHttpMessageConverter.class::cast)
                .forEach(converter -> {
                    converter.setDefaultCharset(StandardCharsets.UTF_8);
                    converter.setWriteAcceptCharset(false);
                });
    }

    static class AsyncAwareSaInterceptor extends SaInterceptor {

        AsyncAwareSaInterceptor() {
            super(handle -> StpUtil.checkLogin());
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                 Object handler) throws Exception {
            if (request.getDispatcherType() == DispatcherType.ASYNC) {
                return true;
            }
            return super.preHandle(request, response, handler);
        }
    }
}
