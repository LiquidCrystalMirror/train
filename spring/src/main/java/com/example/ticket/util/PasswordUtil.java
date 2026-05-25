package com.example.ticket.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PasswordUtil {
    private String salt;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${md5.salt}")
    public void setSalt(String salt) {
        this.salt = salt;
    }
    
    public String md5(String input) {
        return DigestUtils.md5DigestAsHex(input.getBytes());
    }

    // 带盐值的 MD5（更安全）
    public String md5WithSalt(String input) {
        return DigestUtils.md5DigestAsHex((input + salt).getBytes());
    }

    public boolean verifyPassword(String rawPassword, String encryptedPassword) {
        return md5WithSalt(rawPassword).equals(encryptedPassword);
    }
    
    /**
     * 生成随机用户ID
     * 格式：U + 时间戳后6位 + 4位随机数
     * 例如：U2605241234
     */
    public String generateUserId() {
        // 获取当前时间戳的后6位
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
        // 生成4位随机数
        int randomNum = secureRandom.nextInt(9000) + 1000;
        return "U" + timestamp.substring(timestamp.length() - 6) + randomNum;
    }
}
