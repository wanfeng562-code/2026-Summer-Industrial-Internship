package com.alibaba.ticketsystem.utils;

/** CSV 输出工具：处理引号，并阻止 Excel 将用户输入解释为公式。 */
public final class CsvUtils {

    private CsvUtils() {
    }

    public static String cell(String value) {
        String safe = value == null ? "" : value;
        String trimmed = safe.stripLeading();
        if (!trimmed.isEmpty() && "=+-@".indexOf(trimmed.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
