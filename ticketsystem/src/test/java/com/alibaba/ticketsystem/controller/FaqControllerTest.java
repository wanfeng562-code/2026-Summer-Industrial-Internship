package com.alibaba.ticketsystem.controller;

import com.alibaba.ticketsystem.entity.Faq;
import com.alibaba.ticketsystem.mapper.FaqMapper;
import com.alibaba.ticketsystem.service.FaqSemanticService;
import com.alibaba.ticketsystem.service.FaqService;
import com.alibaba.ticketsystem.service.TicketCategoryService;
import com.alibaba.ticketsystem.utils.ApiException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class FaqControllerTest {

    private final FaqController controller =
            new FaqController(mock(FaqService.class), mock(FaqSemanticService.class));

    @Test
    void invalidEnabledValueReturnsBusinessBadRequestInsteadOfServerError() {
        String csv = "category,question,answer,keywords,enabled\n"
                + "LOGISTICS,问题,答案,物流,abc\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "faqs.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() -> controller.importCsv(file))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getMessage()).contains("enabled");
                });
    }

    @Test
    void exportedMultilineCsvCanBeImportedAgain() throws Exception {
        FaqMapper faqMapper = mock(FaqMapper.class);
        FaqService faqService = new FaqService(
                faqMapper,
                mock(TicketCategoryService.class),
                Validation.buildDefaultValidatorFactory().getValidator());
        FaqController realController =
                new FaqController(faqService, mock(FaqSemanticService.class));
        String csv = "category,question,answer,keywords,enabled\n"
                + "\"LOGISTICS\",\"问题\",\"第一行\n第二行\",\"物流\",1\n";
        MockMultipartFile file = new MockMultipartFile(
                "file", "faqs.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        realController.importCsv(file);

        var captor = org.mockito.ArgumentCaptor.forClass(Faq.class);
        verify(faqMapper).insert(captor.capture());
        assertThat(captor.getValue().getAnswer()).isEqualTo("第一行\n第二行");
    }
}
