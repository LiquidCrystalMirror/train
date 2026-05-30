package com.example.ticket.controller;

import com.example.ticket.entity.RefundInfo;
import com.example.ticket.entity.SaleInfo;
import com.example.ticket.entity.User;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.SaleInfoMapper;
import com.example.ticket.service.RefundService;
import com.example.ticket.util.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/refund")
public class RefundController {

    @Resource
    private RefundService refundService;

    @Resource
    private SaleInfoMapper saleInfoMapper;

    /**
     * 退票接口
     */
    @PostMapping("/do")
    public ApiResult<RefundInfo> refund(@RequestBody Map<String, Integer> params, HttpServletRequest request) {
        Integer saleId = params.get("saleId");
        User login = (User) request.getAttribute("auth");
        
        // 校验：非管理员只能退自己的票
        SaleInfo sale = saleInfoMapper.selectBySaleId(saleId);
        if (sale == null) {
            throw new BusinessException(404, "售票记录不存在");
        }
        if (!"admin".equals(login.getRole()) && !login.getUserId().equals(sale.getUserId())) {
            throw new BusinessException(403, "无权操作他人车票");
        }
        
        RefundInfo refundInfo = refundService.refundTicket(saleId);
        return ApiResult.success("退票成功", refundInfo);
    }

    /**
     * 查询退票详情
     */
    @PostMapping("/detail")
    public ApiResult<RefundInfo> getDetail(@RequestBody Map<String, Integer> params) {
        Integer refundId = params.get("refundId");
        if (refundId == null) {
            return ApiResult.error(400, "退票ID不能为空");
        }
        RefundInfo refund = refundService.getById(refundId);
        return refund != null ? ApiResult.success("查询成功", refund) : ApiResult.error(404, "退票记录不存在");
    }
}