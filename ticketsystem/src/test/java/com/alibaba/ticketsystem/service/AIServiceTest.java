package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.SysUser;
import com.alibaba.ticketsystem.utils.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AIServiceTest {

    private final AiModelClient modelClient = mock(AiModelClient.class);
    private final TicketAiContextService contextService = mock(TicketAiContextService.class);
    private final UserService userService = mock(UserService.class);
    private final AIService aiService = new AIService(modelClient, contextService, userService);

    @Test
    void ordinaryChatReturnsModelContent() {
        when(userService.requireCurrentUser()).thenReturn(user());
        when(modelClient.generate("如何申请售后？", 10L)).thenReturn("请在订单详情中发起售后申请。");

        assertThat(aiService.chat("如何申请售后？"))
                .isEqualTo("请在订单详情中发起售后申请。");
    }

    @Test
    void classificationNormalizesKnownCategory() {
        when(modelClient.generate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(" logistics ");

        assertThat(aiService.classify("物流一直没有更新")).isEqualTo("LOGISTICS");
    }

    @Test
    void classificationFallsBackToOtherOnUnexpectedResult() {
        when(modelClient.generate(org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("无法判断");

        assertThat(aiService.classify("帮我看看")).isEqualTo("OTHER");
    }

    @Test
    void modelFailureReturns503WithoutLeakingCause() {
        when(userService.requireCurrentUser()).thenReturn(user());
        when(modelClient.generate("测试", 10L)).thenThrow(new RuntimeException("secret endpoint detail"));

        assertThatThrownBy(() -> aiService.chat("测试"))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
                    assertThat(exception.getMessage()).doesNotContain("secret");
                });
    }

    @Test
    void streamChatForwardsIncrementalChunks() {
        when(userService.requireCurrentUser()).thenReturn(user());
        when(modelClient.stream("流式测试", 10L)).thenReturn(Flux.just("第一段", "第二段"));

        assertThat(aiService.streamChat("流式测试").collectList().block())
                .containsExactly("第一段", "第二段");
    }

    private SysUser user() {
        SysUser user = new SysUser();
        user.setId(10L);
        user.setRole("USER");
        return user;
    }
}
