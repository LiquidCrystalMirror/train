package com.example.ticket.interceptor;

import com.example.ticket.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Value("${my.jwt_pwd}")
    private String jwtpwd;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (req.getMethod().equalsIgnoreCase("options")) {
            return true;
        }

        res.setContentType("application/json;charset=utf-8");
        String jwt = req.getHeader("Authorization");

        if (jwt == null) {
            res.getWriter().write("{\"code\":4001,\"msg\":\"请先登录\",\"data\":null}");
            return false;
        }

        jwt = jwt.substring(7);
        Claims claims;

        try {
            claims = Jwts.parser()
                    .setSigningKey(jwtpwd)
                    .parseClaimsJws(jwt)
                    .getBody();
        } catch (Exception ex) {
            res.getWriter().write("{\"code\":4001,\"msg\":\"凭证无效，过期或被篡改，请重新登录\",\"data\":null}");
            return false;
        }

        // ===================== 完全按照你的 User 实体类赋值 =====================
        User user = new User();
        user.setUserId((Integer) claims.get("userId"));          // 用户ID
        user.setUsername(String.valueOf(claims.get("username"))); // 账号
        user.setPassword(null);                                   // 密码不赋值
        user.setRealName(String.valueOf(claims.get("realName"))); // 真实姓名
        user.setPhone(claims.get("phone") != null ? String.valueOf(claims.get("phone")) : null); // 手机
        user.setRole(String.valueOf(claims.get("role")));         // 角色 admin/user

        // ===================== 存入request，控制器可直接获取 =====================
        req.setAttribute("auth", user);
        return true;
    }
}