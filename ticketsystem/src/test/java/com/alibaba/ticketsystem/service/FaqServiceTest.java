package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.FaqRequest;
import com.alibaba.ticketsystem.entity.Faq;
import com.alibaba.ticketsystem.mapper.FaqMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FaqServiceTest {

    private final FaqMapper faqMapper = mock(FaqMapper.class);
    private final FaqService service = new FaqService(faqMapper);

    @Test
    void createsNormalizedFaq() {
        FaqRequest request = request();

        Faq created = service.create(request);

        ArgumentCaptor<Faq> captor = ArgumentCaptor.forClass(Faq.class);
        verify(faqMapper).insert(captor.capture());
        assertThat(captor.getValue()).isSameAs(created);
        assertThat(created.getCategory()).isEqualTo("LOGISTICS");
        assertThat(created.getQuestion()).isEqualTo("物流为什么没有更新？");
        assertThat(created.getEnabled()).isEqualTo(1);
        assertThat(created.getDeleted()).isZero();
    }

    @Test
    void searchRequiresKeywordOrCategory() {
        assertThatThrownBy(() -> service.search(" ", null))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void updateRejectsDeletedFaq() {
        Faq deleted = new Faq();
        deleted.setDeleted(1);
        when(faqMapper.selectById(1L)).thenReturn(deleted);

        assertThatThrownBy(() -> service.update(1L, request()))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private FaqRequest request() {
        FaqRequest request = new FaqRequest();
        request.setCategory(" logistics ");
        request.setQuestion(" 物流为什么没有更新？ ");
        request.setAnswer(" 请先核对物流单号，长时间未更新可转人工。 ");
        request.setKeywords("物流,延迟,单号");
        request.setEnabled(1);
        return request;
    }
}
