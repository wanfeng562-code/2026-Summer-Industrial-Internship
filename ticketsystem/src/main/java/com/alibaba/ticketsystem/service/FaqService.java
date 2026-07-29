package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.FaqRequest;
import com.alibaba.ticketsystem.entity.Faq;
import com.alibaba.ticketsystem.mapper.FaqMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqMapper faqMapper;
    private final TicketCategoryService categoryService;
    private final Validator validator;

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
        validateRequest(request);
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
        validateRequest(request);
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

    public List<Faq> exportAll() {
        return faqMapper.selectList(new QueryWrapper<Faq>()
                .eq("deleted", 0).orderByAsc("id"));
    }

    @Transactional
    public int importRows(List<FaqRequest> requests) {
        int count = 0;
        for (FaqRequest request : requests) {
            validateRequest(request);
            Faq faq = new Faq();
            apply(faq, request);
            faq.setDeleted(0);
            faq.setCreateTime(LocalDateTime.now());
            faq.setUpdateTime(LocalDateTime.now());
            faqMapper.insert(faq);
            count++;
        }
        return count;
    }

    /** 支持标准 CSV 的引号与双引号转义。 */
    public List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                fields.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        if (quoted) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CSV 格式错误：引号未闭合");
        }
        fields.add(current.toString().trim());
        return fields;
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

    private void validateRequest(FaqRequest request) {
        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "FAQ 数据校验失败：" + violations.iterator().next().getMessage());
        }
    }

    private String normalizeCategory(String category) {
        String value = category == null ? "" : category.trim().toUpperCase(Locale.ROOT);
        categoryService.requireActive(value);
        return value;
    }
}
