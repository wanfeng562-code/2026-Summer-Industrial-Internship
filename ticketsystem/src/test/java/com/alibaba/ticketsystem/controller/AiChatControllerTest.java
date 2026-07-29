package com.alibaba.ticketsystem.controller;

import com.alibaba.ticketsystem.dto.AiChatRequest;
import com.alibaba.ticketsystem.service.AIService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiChatControllerTest {

    @Test
    void streamProducesCompleteSseFramesThatPreserveNewlines() {
        AIService aiService = mock(AIService.class);
        when(aiService.streamChat("测试")).thenReturn(Flux.just("你", "好\n世界"));
        AiChatController controller = new AiChatController(aiService);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AiChatRequest request = new AiChatRequest();
        request.setMessage("测试");

        List<ServerSentEvent<String>> frames = controller.stream(request, response).collectList().block();

        assertThat(frames).extracting(ServerSentEvent::event)
                .containsExactly("message", "message", "done");
        assertThat(frames).extracting(ServerSentEvent::data)
                .containsExactly("你", "好\n世界", "[DONE]");
    }

    @Test
    void streamFailureIsConvertedToVisibleErrorFrame() {
        AIService aiService = mock(AIService.class);
        when(aiService.streamChat("测试")).thenReturn(Flux.error(new IllegalStateException("upstream")));
        AiChatController controller = new AiChatController(aiService);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AiChatRequest request = new AiChatRequest();
        request.setMessage("测试");

        List<ServerSentEvent<String>> frames = controller.stream(request, response).collectList().block();

        assertThat(frames).extracting(ServerSentEvent::event).containsExactly("error");
        assertThat(frames).extracting(ServerSentEvent::data)
                .containsExactly("AI服务暂时不可用，请稍后重试");
    }

    @Test
    void streamIsEncodedOnceAsValidSseOnTheHttpLayer() throws Exception {
        AIService aiService = mock(AIService.class);
        when(aiService.streamChat("测试")).thenReturn(Flux.just("hello", "!"));
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new AiChatController(aiService))
                .build();

        MvcResult initialResult = mockMvc.perform(post("/ai/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"测试\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        MvcResult completedResult = mockMvc.perform(asyncDispatch(initialResult))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andReturn();

        assertThat(completedResult.getResponse().getContentAsString(StandardCharsets.UTF_8))
                .isEqualTo("event:message\ndata:hello\n\n"
                        + "event:message\ndata:!\n\n"
                        + "event:done\ndata:[DONE]\n\n");
    }
}
