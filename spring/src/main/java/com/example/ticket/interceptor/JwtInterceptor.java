package com.example.ticket.interceptor;

import com.example.ticket.entity.User;
import com.example.ticket.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (req.getMethod().equalsIgnoreCase("options")) {
            return true;
        }

        res.setContentType("application/json;charset=utf-8");
        String jwt = req.getHeader("Authorization");

        if (jwt == null) {
            res.getWriter().write("{\"code\":401,\"msg\":\"请先登录\",\"data\":null}");
            return false;
        }

        // 去除 "Bearer " 前缀
        jwt = jwt.substring(7);
        
        // 使用JwtUtil解析和验证Token
        User user = jwtUtil.parseAndValidateToken(jwt);
        
        if (user == null) {
            res.getWriter().write("{\"code\":401,\"msg\":\"凭证无效，过期或被篡改，请重新登录\",\"data\":null}");
            return false;
        }

        // 存入request，控制器可直接获取
        req.setAttribute("auth", user);
        return true;
    }
}