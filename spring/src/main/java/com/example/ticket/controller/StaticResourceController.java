package com.example.ticket.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 静态资源控制器
 * 用于处理浏览器自动请求的 favicon 等静态资源，避免报错
 */
@Controller
public class StaticResourceController {

    /**
     * 处理 /favicon.ico 请求（浏览器默认行为）
     */
    @ResponseBody
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> faviconIco() {
        return ResponseEntity.noContent().build();
    }

    /**
     * 处理 /favicon.svg 请求（vue/index.html 中 <link rel="icon"> 引用）
     */
    @ResponseBody
    @GetMapping("/favicon.svg")
    public ResponseEntity<Void> faviconSvg() {
        return ResponseEntity.noContent().build();
    }
}
