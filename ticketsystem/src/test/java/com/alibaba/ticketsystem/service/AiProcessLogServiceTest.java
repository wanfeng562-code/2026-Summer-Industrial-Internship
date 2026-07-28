package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.AiProcessLog;
import com.alibaba.ticketsystem.mapper.AiProcessLogMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AiProcessLogServiceTest {

    @Test
    void recordsSanitizedAiProcessingResult() {
        AiProcessLogMapper mapper = mock(AiProcessLogMapper.class);
        AiProcessLogService service = new AiProcessLogService(mapper);

        service.record(1L, 2L, "LOGISTICS", "AUTO_REPLY",
                "请核对物流单号", "受控AI回复", System.currentTimeMillis());

        ArgumentCaptor<AiProcessLog> captor = ArgumentCaptor.forClass(AiProcessLog.class);
        verify(mapper).insert(captor.capture());
        assertThat(captor.getValue().getTicketId()).isEqualTo(1L);
        assertThat(captor.getValue().getMessageId()).isEqualTo(2L);
        assertThat(captor.getValue().getAiAction()).isEqualTo("AUTO_REPLY");
        assertThat(captor.getValue().getCreateTime()).isNotNull();
    }
}
