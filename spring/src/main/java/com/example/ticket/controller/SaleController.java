package com.example.ticket.controller;

import com.example.ticket.entity.SaleInfo;
import com.example.ticket.entity.User;
import com.example.ticket.service.SaleService;
import com.example.ticket.util.RespEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/sale")
public class SaleController {

    @Resource
    private SaleService saleService;

    /**
     * 售票接口
     */
    @PostMapping("/do")
    public RespEntity sell(@RequestBody SaleInfo saleInfo, HttpServletRequest request) {
        User login = (User) request.getAttribute("auth");
        
        // 调用Service层业务方法，异常由全局处理器处理
        Integer saleId = saleService.sellTicket(saleInfo, login.getUserId());
        
        return new RespEntity(2000, "售票成功", saleId);
    }
}