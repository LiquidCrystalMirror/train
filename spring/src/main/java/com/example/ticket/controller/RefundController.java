package com.example.ticket.controller;

import com.example.ticket.entity.RefundInfo;
import com.example.ticket.service.RefundService;
import com.example.ticket.util.ApiResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/refund")
public class RefundController {

    @Resource
    private RefundService refundService;

    /**
     * 退票接口
     */
    @PostMapping("/do")
    public ApiResult<RefundInfo> refund(@RequestBody Map<String, Integer> params) {
        Integer saleId = params.get("saleId");
        
        // 调用Service层业务方法，异常由全局处理器处理
        RefundInfo refundInfo = refundService.refundTicket(saleId);
        
        return ApiResult.success("退票成功", refundInfo);
    }
}