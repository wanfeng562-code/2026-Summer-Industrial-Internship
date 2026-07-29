package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.FaqSemanticConfigRequest;
import com.alibaba.ticketsystem.entity.Faq;
import com.alibaba.ticketsystem.entity.FaqSemanticConfig;
import com.alibaba.ticketsystem.mapper.FaqMapper;
import com.alibaba.ticketsystem.mapper.FaqSemanticConfigMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.vo.FaqSemanticSearchVo;
import com.alibaba.ticketsystem.vo.SemanticFaqResultVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaqSemanticService {
    private final FaqSemanticConfigMapper configMapper;
    private final FaqMapper faqMapper;
    private final AiModelClient modelClient;
    private final FaqService faqService;
    private final TicketCategoryService categoryService;

    public FaqSemanticConfig getConfig() {
        FaqSemanticConfig config = configMapper.selectById(1L);
        if (config != null) return config;
        config = new FaqSemanticConfig();
        config.setId(1L);
        config.setEnabled(0);
        config.setSimilarityThreshold(new BigDecimal("0.650"));
        config.setMaxCandidates(30);
        config.setMaxResults(5);
        config.setUpdateTime(LocalDateTime.now());
        configMapper.insert(config);
        return config;
    }

    @Transactional
    public FaqSemanticConfig updateConfig(FaqSemanticConfigRequest request) {
        if (request.getEnabled() != 0 && request.getEnabled() != 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "enabled 只能是 0 或 1");
        }
        FaqSemanticConfig config = getConfig();
        config.setEnabled(request.getEnabled());
        config.setSimilarityThreshold(request.getSimilarityThreshold());
        config.setMaxCandidates(request.getMaxCandidates());
        config.setMaxResults(request.getMaxResults());
        config.setUpdateTime(LocalDateTime.now());
        configMapper.updateById(config);
        return config;
    }

    public FaqSemanticSearchVo search(String question, String category) {
        if (question == null || question.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "语义检索问题不能为空");
        }
        FaqSemanticConfig config = getConfig();
        if (!Integer.valueOf(1).equals(config.getEnabled())) {
            throw new ApiException(HttpStatus.CONFLICT, "FAQ 大模型语义检索尚未启用");
        }
        QueryWrapper<Faq> query = new QueryWrapper<Faq>().eq("deleted", 0).eq("enabled", 1);
        if (category != null && !category.isBlank()) {
            query.eq("category", categoryService.requireActive(category).getCategoryCode());
        }
        List<Faq> candidates = faqMapper.selectList(query.orderByDesc("id")
                .last("LIMIT " + config.getMaxCandidates()));
        if (candidates.isEmpty()) return new FaqSemanticSearchVo("LLM", List.of());

        try {
            String response = modelClient.generate(buildPrompt(question, candidates, config));
            return new FaqSemanticSearchVo("LLM", parseScores(response, candidates, config));
        } catch (Exception exception) {
            log.warn("FAQ semantic search failed, falling back to keyword search: {}", exception.getMessage());
            List<SemanticFaqResultVo> fallback = faqService.search(question, category).stream()
                    .limit(config.getMaxResults())
                    .map(faq -> new SemanticFaqResultVo(faq, BigDecimal.ZERO))
                    .toList();
            return new FaqSemanticSearchVo("KEYWORD_FALLBACK", fallback);
        }
    }

    private String buildPrompt(String question, List<Faq> candidates, FaqSemanticConfig config) {
        StringBuilder items = new StringBuilder();
        for (Faq faq : candidates) {
            items.append(faq.getId()).append(" | Q: ").append(limit(faq.getQuestion(), 300))
                    .append(" | A: ").append(limit(faq.getAnswer(), 500)).append('\n');
        }
        return """
                You are an FAQ semantic similarity scorer. Compare the user question with each FAQ.
                Output only lines in the exact form: FAQ_ID|SCORE
                SCORE must be between 0 and 1. Output at most %d rows, highest score first.
                Do not invent IDs. Omit rows below %s.

                User question: %s

                FAQ candidates:
                %s
                """.formatted(config.getMaxResults(), config.getSimilarityThreshold(), question, items);
    }

    private List<SemanticFaqResultVo> parseScores(String response, List<Faq> candidates,
                                                   FaqSemanticConfig config) {
        Map<Long, Faq> byId = new LinkedHashMap<>();
        candidates.forEach(faq -> byId.put(faq.getId(), faq));
        List<SemanticFaqResultVo> results = new ArrayList<>();
        Set<Long> seenIds = new HashSet<>();
        for (String line : response.split("\\R")) {
            String[] parts = line.trim().split("\\|");
            if (parts.length != 2) continue;
            try {
                Long id = Long.valueOf(parts[0].trim());
                BigDecimal score = new BigDecimal(parts[1].trim());
                Faq faq = byId.get(id);
                if (faq != null && seenIds.add(id)
                        && score.compareTo(config.getSimilarityThreshold()) >= 0
                        && score.compareTo(BigDecimal.ONE) <= 0 && score.signum() >= 0) {
                    results.add(new SemanticFaqResultVo(faq, score));
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed model lines and keep valid scored rows.
            }
        }
        return results.stream()
                .sorted(Comparator.comparing(SemanticFaqResultVo::getSimilarity).reversed())
                .limit(config.getMaxResults())
                .toList();
    }

    private String limit(String value, int max) {
        if (value == null || value.length() <= max) return value;
        return value.substring(0, max) + "...";
    }
}
