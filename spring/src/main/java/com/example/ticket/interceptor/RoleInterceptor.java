package com.example.ticket.interceptor;

import com.example.ticket.entity.User;
import com.example.ticket.util.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.annotation.*;

/**
 * 角色权限拦截器
 * 只在JWT验证通过后执行，检查用户是否有足够的权限
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法级别的请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查方法或类上是否有@RequireRole注解
        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        
        // 没有注解，不需要权限检查
        if (requireRole == null) {
            return true;
        }

        // 从request中获取JWT验证后的用户信息
        User user = (User) request.getAttribute("auth");
        if (user == null) {
            // JWT拦截器应该已经处理了这种情况
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(ApiResult.error(401, "请先登录").toString());
            return false;
        }

        // 检查用户角色是否符合要求
        String requiredRole = requireRole.value();
        if (!requiredRole.equals(user.getRole())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(ApiResult.error(403, "无权限访问").toString());
            return false;
        }

        return true;
    }

    /**
     * 角色权限注解
     * 用于标记需要特定角色才能访问的方法或类
     */
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface RequireRole {
        /**
         * 需要的角色
         * @return 角色名称（admin 或 user）
         */
        String value();
    }
}
