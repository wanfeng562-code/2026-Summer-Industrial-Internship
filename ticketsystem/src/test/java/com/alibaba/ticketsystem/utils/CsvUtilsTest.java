package com.alibaba.ticketsystem.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CsvUtilsTest {

    @Test
    void escapesQuotesAndNeutralizesSpreadsheetFormula() {
        assertThat(CsvUtils.cell("正常\"标题")).isEqualTo("\"正常\"\"标题\"");
        assertThat(CsvUtils.cell("=HYPERLINK(\"https://example.com\")"))
                .isEqualTo("\"'=HYPERLINK(\"\"https://example.com\"\")\"");
        assertThat(CsvUtils.cell("  +1+1")).isEqualTo("\"'  +1+1\"");
    }
}
