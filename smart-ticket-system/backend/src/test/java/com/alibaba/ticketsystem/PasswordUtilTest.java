package com.alibaba.ticketsystem;

import com.alibaba.ticketsystem.utils.PasswordUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void bcryptEncodeAndMatch() {
        String encoded = PasswordUtil.encode("123456");
        Assertions.assertTrue(PasswordUtil.isBcrypt(encoded));
        Assertions.assertTrue(PasswordUtil.matches("123456", encoded));
        Assertions.assertFalse(PasswordUtil.matches("wrong", encoded));
    }

    @Test
    void plaintextCompatibility() {
        Assertions.assertTrue(PasswordUtil.matches("123456", "123456"));
        Assertions.assertFalse(PasswordUtil.matches("123456", "654321"));
    }
}
