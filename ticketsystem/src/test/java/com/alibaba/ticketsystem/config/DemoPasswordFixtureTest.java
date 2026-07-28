package com.alibaba.ticketsystem.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class DemoPasswordFixtureTest {

    private static final Pattern BCRYPT_HASH =
            Pattern.compile("\\$2[ayb]\\$\\d{2}\\$[A-Za-z0-9./]{53}");

    @Test
    void allDemoAccountHashesMatchDocumentedPassword() throws IOException {
        String sql = new ClassPathResource("static/data.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        Matcher matcher = BCRYPT_HASH.matcher(sql);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        int hashCount = 0;

        while (matcher.find()) {
            hashCount++;
            assertThat(encoder.matches("123456", matcher.group())).isTrue();
        }

        assertThat(hashCount).isEqualTo(6);
    }
}
