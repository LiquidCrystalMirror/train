package com.example.ticket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 静态资源控制器
 * 用于处理浏览器自动请求的 favicon.ico 等静态资源，避免报错
 */
@Controller
public class StaticResourceController {

    /**
     * 处理 favicon.ico 请求
     * 返回空响应，避免 NoResourceFoundException
     */
    @GetMapping("/favicon.ico")
    public void favicon() {
        // 空方法，返回 200 OK
    }
}
