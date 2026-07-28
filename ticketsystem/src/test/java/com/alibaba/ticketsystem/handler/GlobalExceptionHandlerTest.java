package com.alibaba.ticketsystem.handler;

import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.utils.R;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void apiExceptionUsesSameHttpAndBusinessCode() {
        ResponseEntity<R<?>> response = handler.handleApiException(
                new ApiException(HttpStatus.FORBIDDEN, "无权访问该订单"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(403);
        assertThat(response.getBody().getMsg()).isEqualTo("无权访问该订单");
    }

    @Test
    void duplicateKeyUsesConflictResponse() {
        ResponseEntity<R<?>> response = handler.handleDuplicateKey(
                new DuplicateKeyException("duplicate"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(409);
    }
}
