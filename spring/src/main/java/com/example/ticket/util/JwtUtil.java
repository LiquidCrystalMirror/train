package com.example.ticket.util;

import com.example.ticket.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 负责JWT Token的生成、解析和验证
 */
@Component
public class JwtUtil {

    @Value("${my.jwt_pwd}")
    private String jwtSecret;

    // Token有效期：24小时（毫秒）
    private static final long TOKEN_EXPIRATION = 86400000L;

    /**
     * 生成JWT Token
     *
     * @param user 用户对象
     * @return JWT Token字符串
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("realName", user.getRealName());
        claims.put("phone", user.getPhone());
        claims.put("role", user.getRole());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    /**
     * 解析JWT Token
     *
     * @param token JWT Token字符串
     * @return Claims对象
     * @throws Exception 解析失败时抛出异常
     */
    public Claims parseToken(String token) throws Exception {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从Token中提取用户ID
     *
     * @param token JWT Token字符串
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return String.valueOf(claims.get("userId"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中提取用户名
     *
     * @param token JWT Token字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return String.valueOf(claims.get("username"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中提取角色
     *
     * @param token JWT Token字符串
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return String.valueOf(claims.get("role"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中解析并验证，返回User对象
     * 如果Token无效或过期，返回null
     *
     * @param token JWT Token字符串（不包含"Bearer "前缀）
     * @return User对象，如果Token无效则返回null
     */
    public User parseAndValidateToken(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        
        try {
            Claims claims = parseToken(token);
            
            // 检查是否过期
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return null; // Token已过期
            }
            
            // 构建User对象
            User user = new User();
            user.setUserId(String.valueOf(claims.get("userId")));
            user.setUsername(String.valueOf(claims.get("username")));
            user.setRealName(String.valueOf(claims.get("realName")));
            user.setPhone(claims.get("phone") != null ? String.valueOf(claims.get("phone")) : null);
            user.setRole(String.valueOf(claims.get("role")));
            user.setPassword(null); // 不设置密码
            
            return user;
        } catch (Exception e) {
            // Token解析失败（签名错误、格式错误等）
            return null;
        }
    }

    /**
     * 验证Token是否有效
     *
     * @param token JWT Token字符串
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查Token是否过期
     *
     * @param token JWT Token字符串
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // 解析失败视为过期
        }
    }
}
