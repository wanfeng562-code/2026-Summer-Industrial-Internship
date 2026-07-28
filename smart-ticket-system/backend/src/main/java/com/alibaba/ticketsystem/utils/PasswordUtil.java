package com.alibaba.ticketsystem.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码哈希与校验（BCrypt）
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (isBcrypt(encodedPassword)) {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        }
        // 兼容尚未迁移的明文演示账号
        return encodedPassword.equals(rawPassword);
    }

    public static boolean isBcrypt(String password) {
        return password != null
                && (password.startsWith("$2a$")
                || password.startsWith("$2b$")
                || password.startsWith("$2y$"));
    }
}
