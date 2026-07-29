package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.entity.Faq;
import com.alibaba.ticketsystem.entity.FaqSemanticConfig;
import com.alibaba.ticketsystem.mapper.FaqMapper;
import com.alibaba.ticketsystem.mapper.FaqSemanticConfigMapper;
import com.alibaba.ticketsystem.vo.FaqSemanticSearchVo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FaqSemanticServiceTest {

    @Test
    void modelScoresAreDeduplicatedSortedAndLimited() {
        FaqSemanticConfigMapper configMapper = mock(FaqSemanticConfigMapper.class);
        FaqMapper faqMapper = mock(FaqMapper.class);
        AiModelClient modelClient = mock(AiModelClient.class);
        FaqSemanticConfig config = config();
        when(configMapper.selectById(1L)).thenReturn(config);
        when(faqMapper.selectList(any())).thenReturn(List.of(faq(1L), faq(2L)));
        when(modelClient.generate(any())).thenReturn("1|0.70\n2|0.90\n2|1.00");
        FaqSemanticService service = new FaqSemanticService(
                configMapper, faqMapper, modelClient, mock(FaqService.class),
                mock(TicketCategoryService.class));

        FaqSemanticSearchVo result = service.search("物流为什么没更新", null);

        assertThat(result.getResults()).extracting(item -> item.getFaq().getId())
                .containsExactly(2L, 1L);
        assertThat(result.getResults()).extracting(item -> item.getSimilarity())
                .containsExactly(new BigDecimal("0.90"), new BigDecimal("0.70"));
    }

    private FaqSemanticConfig config() {
        FaqSemanticConfig config = new FaqSemanticConfig();
        config.setId(1L);
        config.setEnabled(1);
        config.setSimilarityThreshold(new BigDecimal("0.50"));
        config.setMaxCandidates(30);
        config.setMaxResults(5);
        return config;
    }

    private Faq faq(Long id) {
        Faq faq = new Faq();
        faq.setId(id);
        faq.setQuestion("问题" + id);
        faq.setAnswer("答案" + id);
        return faq;
    }
}
