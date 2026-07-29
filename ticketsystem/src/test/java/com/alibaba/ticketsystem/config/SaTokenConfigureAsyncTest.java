package com.alibaba.ticketsystem.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SaTokenConfigureAsyncTest {

    @Test
    void asyncRedispatchSkipsRepeatedSaTokenContextAccess() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getDispatcherType()).thenReturn(DispatcherType.ASYNC);

        boolean allowed = new SaTokenConfigure.AsyncAwareSaInterceptor()
                .preHandle(request, response, new Object());

        assertThat(allowed).isTrue();
    }

    @Test
    void plainTextAndSseDataUseUtf8ByDefault() {
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter();
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(stringConverter);

        new SaTokenConfigure().extendMessageConverters(converters);

        assertThat(stringConverter.getDefaultCharset()).isEqualTo(StandardCharsets.UTF_8);
    }
}
