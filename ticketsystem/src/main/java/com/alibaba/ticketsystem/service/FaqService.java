package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.FaqRequest;
import com.alibaba.ticketsystem.entity.Faq;
import com.alibaba.ticketsystem.mapper.FaqMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FaqService {

    private static final List<String> CATEGORIES =
            List.of("REFUND", "LOGISTICS", "DAMAGE", "INVOICE", "OTHER");

    private final FaqMapper faqMapper;

    public Page<Faq> page(int current, int size, String category, Integer enabled, String keyword) {
        QueryWrapper<Faq> query = baseQuery(category, enabled, keyword)
                .orderByDesc("id");
        return faqMapper.selectPage(new Page<>(current, size), query);
    }

    public List<Faq> search(String keyword, String category) {
        if (!StringUtils.hasText(keyword) && !StringUtils.hasText(category)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "关键词和分类至少填写一项");
        }
        return faqMapper.selectList(baseQuery(category, 1, keyword)
                .orderByDesc("id")
                .last("LIMIT 20"));
    }

    public Faq get(Long id) {
        Faq faq = faqMapper.selectById(id);
        if (faq == null || Integer.valueOf(1).equals(faq.getDeleted())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "FAQ不存在");
        }
        return faq;
    }

    @Transactional
    public Faq create(FaqRequest request) {
        Faq faq = new Faq();
        apply(faq, request);
        faq.setDeleted(0);
        faq.setCreateTime(LocalDateTime.now());
        faq.setUpdateTime(LocalDateTime.now());
        faqMapper.insert(faq);
        return faq;
    }

    @Transactional
    public Faq update(Long id, FaqRequest request) {
        Faq faq = get(id);
        apply(faq, request);
        faq.setUpdateTime(LocalDateTime.now());
        faqMapper.updateById(faq);
        return faq;
    }

    @Transactional
    public void delete(Long id) {
        Faq faq = get(id);
        faq.setDeleted(1);
        faq.setUpdateTime(LocalDateTime.now());
        faqMapper.updateById(faq);
    }

    private QueryWrapper<Faq> baseQuery(String category, Integer enabled, String keyword) {
        QueryWrapper<Faq> query = new QueryWrapper<Faq>().eq("deleted", 0);
        if (StringUtils.hasText(category)) {
            query.eq("category", normalizeCategory(category));
        }
        if (enabled != null) {
            if (enabled != 0 && enabled != 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "启用状态只能是0或1");
            }
            query.eq("enabled", enabled);
        }
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            query.and(wrapper -> wrapper
                    .like("question", value)
                    .or().like("answer", value)
                    .or().like("keywords", value));
        }
        return query;
    }

    private void apply(Faq faq, FaqRequest request) {
        faq.setCategory(normalizeCategory(request.getCategory()));
        faq.setQuestion(request.getQuestion().trim());
        faq.setAnswer(request.getAnswer().trim());
        faq.setKeywords(StringUtils.hasText(request.getKeywords())
                ? request.getKeywords().trim() : null);
        faq.setEnabled(request.getEnabled());
    }

    private String normalizeCategory(String category) {
        String value = category == null ? "" : category.trim().toUpperCase(Locale.ROOT);
        if (!CATEGORIES.contains(value)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FAQ分类不正确");
        }
        return value;
    }
}
