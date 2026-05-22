package com.example.ticket.controller;

import com.example.ticket.entity.RefundInfo;
import com.example.ticket.entity.SaleInfo;
import com.example.ticket.mapper.RefundInfoMapper;
import com.example.ticket.mapper.SaleInfoMapper;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.service.RefundService;
import com.example.ticket.service.SaleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/refund")
public class RefundController {

    @Resource
    private RefundService refundService;

    @Resource
    private SaleService saleService;

    @Resource
    private TicketInfoMapper ticketInfoMapper;
    
    @Resource
    private SaleInfoMapper saleInfoMapper;
    
    @Resource
    private RefundInfoMapper refundInfoMapper;

    @PostMapping("/do")
    public Map<String, Object> refund(@RequestBody Map<String, Integer> params) {
        Integer saleId = params.get("saleId");
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 查售票记录
            SaleInfo saleInfo = saleInfoMapper.selectBySaleId(saleId);
            if (saleInfo == null) {
                result.put("code", 500);
                result.put("msg", "退款失败：未找到对应销售记录");
                return result;
            }

            // 2. 不能重复退
            if ("已退票".equals(saleInfo.getSaleStatus())) {
                result.put("code", 500);
                result.put("msg", "退款失败：该票已退票");
                return result;
            }

            // 3. 新增退票记录
            RefundInfo refundInfo = new RefundInfo();
            refundInfo.setSaleId(saleInfo.getSaleId());
            refundInfo.setTicketId(saleInfo.getTicketId());
            refundInfo.setTrainId(saleInfo.getTrainId());
            refundInfo.setUserId(saleInfo.getUserId());
            refundInfo.setRefundTime(LocalDateTime.now());
            refundInfo.setRefundStatus("已完成");
            refundInfo.setRefundRemark("用户申请退票");
            refundInfo.setCreateTime(LocalDateTime.now());
            refundInfoMapper.insertRefund(refundInfo);

            // 4. 把 sale_info 改为 已退票
            saleInfoMapper.updateSaleStatus(saleInfo.getSaleId(), "已退票");

            // 5. 把车票状态改回 可售
            ticketInfoMapper.updateTicketStatus(saleInfo.getTicketId(), "可售");

            result.put("code", 200);
            result.put("msg", "退票成功");
            result.put("data", refundInfo);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "退票异常：" + e.getMessage());
        }
        return result;
    }
}