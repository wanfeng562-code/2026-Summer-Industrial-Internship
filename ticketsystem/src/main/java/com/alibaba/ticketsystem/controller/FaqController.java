package com.alibaba.ticketsystem.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.ticketsystem.dto.FaqRequest;
import com.alibaba.ticketsystem.dto.FaqSemanticConfigRequest;
import com.alibaba.ticketsystem.service.FaqService;
import com.alibaba.ticketsystem.service.FaqSemanticService;
import com.alibaba.ticketsystem.utils.R;
import com.alibaba.ticketsystem.utils.ApiException;
import com.alibaba.ticketsystem.utils.CsvUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/faqs")
public class FaqController {

    private final FaqService faqService;
    private final FaqSemanticService semanticService;

    @GetMapping("/search")
    @SaCheckPermission("faq:query")
    public R<?> search(
            @RequestParam(required = false) @Size(max = 200) String keyword,
            @RequestParam(required = false) @Size(max = 30) String category) {
        return R.success("FAQ检索成功", faqService.search(keyword, category));
    }

    @GetMapping("/semantic-search")
    @SaCheckPermission("faq:query")
    public R<?> semanticSearch(@RequestParam @NotBlank @Size(max = 500) String question,
                               @RequestParam(required = false) @Size(max = 30) String category) {
        return R.success("FAQ语义检索成功", semanticService.search(question, category));
    }

    @GetMapping("/semantic-config")
    @SaCheckPermission("faq:manage")
    public R<?> semanticConfig() {
        return R.success("FAQ语义检索配置查询成功", semanticService.getConfig());
    }

    @PutMapping("/semantic-config")
    @SaCheckPermission("faq:manage")
    public R<?> updateSemanticConfig(@Valid @RequestBody FaqSemanticConfigRequest request) {
        return R.success("FAQ语义检索配置已更新", semanticService.updateConfig(request));
    }

    @GetMapping
    @SaCheckPermission("faq:manage")
    public R<?> page(
            @RequestParam(defaultValue = "1") @Min(1) Integer current,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer size,
            @RequestParam(required = false) @Size(max = 30) String category,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(required = false) @Size(max = 200) String keyword) {
        return R.success("FAQ分页查询成功",
                faqService.page(current, size, category, enabled, keyword));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("faq:manage")
    public R<?> get(@PathVariable Long id) {
        return R.success("FAQ查询成功", faqService.get(id));
    }

    @PostMapping
    @SaCheckPermission("faq:manage")
    public R<?> create(@Valid @RequestBody FaqRequest request) {
        return R.success("FAQ创建成功", faqService.create(request));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("faq:manage")
    public R<?> update(@PathVariable Long id, @Valid @RequestBody FaqRequest request) {
        return R.success("FAQ修改成功", faqService.update(id, request));
    }

    @PostMapping("/import")
    @SaCheckPermission("faq:manage")
    public R<?> importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请选择包含 FAQ 数据的 CSV 文件");
        }
        List<FaqRequest> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            int recordStartLine = 0;
            StringBuilder record = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                if (record.isEmpty() && line.isBlank()) {
                    continue;
                }
                if (record.isEmpty()) {
                    recordStartLine = lineNumber;
                } else {
                    record.append('\n');
                }
                record.append(line);
                if (!isCompleteCsvRecord(record)) {
                    continue;
                }
                List<String> values = faqService.parseCsvLine(record.toString());
                record.setLength(0);
                if (values.size() != 5) {
                    throw new ApiException(HttpStatus.BAD_REQUEST,
                            "CSV 第 " + recordStartLine
                                    + " 行开始的记录必须包含 category,question,answer,keywords,enabled 五列");
                }
                FaqRequest request = new FaqRequest();
                request.setCategory(values.get(0));
                request.setQuestion(values.get(1));
                request.setAnswer(values.get(2));
                request.setKeywords(values.get(3));
                try {
                    int enabled = Integer.parseInt(values.get(4));
                    if (enabled != 0 && enabled != 1) {
                        throw new NumberFormatException();
                    }
                    request.setEnabled(enabled);
                } catch (NumberFormatException exception) {
                    throw new ApiException(HttpStatus.BAD_REQUEST,
                            "CSV 第 " + recordStartLine + " 行 enabled 只能是 0 或 1");
                }
                rows.add(request);
            }
            if (!record.isEmpty()) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "CSV 第 " + recordStartLine + " 行开始的记录存在未闭合引号");
            }
        }
        if (rows.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CSV 文件中没有可导入的 FAQ 数据");
        }
        return R.success("FAQ 批量导入成功", faqService.importRows(rows));
    }

    @GetMapping("/export")
    @SaCheckPermission("faq:manage")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=faqs.csv");
        try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write('\ufeff');
            writer.write("category,question,answer,keywords,enabled\n");
            for (var faq : faqService.exportAll()) {
                writer.write(CsvUtils.cell(faq.getCategory()) + "," + CsvUtils.cell(faq.getQuestion()) + ","
                        + CsvUtils.cell(faq.getAnswer()) + "," + CsvUtils.cell(faq.getKeywords())
                        + "," + faq.getEnabled() + "\n");
            }
        }
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("faq:manage")
    public R<?> delete(@PathVariable Long id) {
        faqService.delete(id);
        return R.success("FAQ删除成功");
    }

    private boolean isCompleteCsvRecord(CharSequence record) {
        boolean quoted = false;
        for (int index = 0; index < record.length(); index++) {
            if (record.charAt(index) != '"') {
                continue;
            }
            if (quoted && index + 1 < record.length() && record.charAt(index + 1) == '"') {
                index++;
            } else {
                quoted = !quoted;
            }
        }
        return !quoted;
    }
}
