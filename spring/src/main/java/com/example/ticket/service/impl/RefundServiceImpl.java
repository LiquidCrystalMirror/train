package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.RefundInfo;
import com.example.ticket.entity.SaleInfo;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.RefundInfoMapper;
import com.example.ticket.mapper.SaleInfoMapper;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.service.RefundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class RefundServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundService {
    
    @Resource
    private SaleInfoMapper saleInfoMapper;
    
    @Resource
    private TicketInfoMapper ticketInfoMapper;
    
    /**
     * 退票业务方法（带事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundInfo refundTicket(Integer saleId) {
        if (saleId == null) {
            throw new BusinessException("售票记录ID不能为空");
        }
        
        // 1. 查售票记录
        SaleInfo saleInfo = saleInfoMapper.selectBySaleId(saleId);
        if (saleInfo == null) {
            throw new BusinessException(404, "未找到对应销售记录");
        }
        
        // 2. 不能重复退
        if ("已退票".equals(saleInfo.getSaleStatus())) {
            throw new BusinessException("该票已退票，不能重复退票");
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
        
        int insertResult = this.baseMapper.insertRefund(refundInfo);
        if (insertResult <= 0) {
            throw new BusinessException("创建退票记录失败");
        }
        
        // 4. 把 sale_info 改为 已退票
        int updateSaleResult = saleInfoMapper.updateSaleStatus(saleInfo.getSaleId(), "已退票");
        if (updateSaleResult <= 0) {
            throw new BusinessException("更新售票记录状态失败");
        }
        
        // 5. 把车票状态改回 可售
        int updateTicketResult = ticketInfoMapper.updateTicketStatus(saleInfo.getTicketId(), "可售");
        if (updateTicketResult <= 0) {
            throw new BusinessException("更新车票状态失败");
        }
        
        return refundInfo;
    }
}