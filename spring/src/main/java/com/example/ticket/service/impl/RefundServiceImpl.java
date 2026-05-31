package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.RefundInfo;
import com.example.ticket.entity.SaleInfo;
import com.example.ticket.entity.TicketInfo;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.RefundInfoMapper;
import com.example.ticket.mapper.SaleInfoMapper;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.mapper.TicketInventoryMapper;
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
    private TicketInventoryMapper inventoryMapper;

    @Resource
    private TicketInfoMapper ticketInfoMapper;
    
    /**
     * 退票业务方法（带事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundInfo refundTicket(Integer saleId) {
        // 1. 查询销售记录
        SaleInfo saleInfo = saleInfoMapper.selectBySaleId(saleId);
        if (saleInfo == null) throw new BusinessException(404, "未找到对应销售记录");
        if ("已退票".equals(saleInfo.getSaleStatus())) {
            throw new BusinessException("该票已退票，不能重复退票");
        }

        // 2. 查询车票信息
        TicketInfo ticket = ticketInfoMapper.selectById(saleInfo.getTicketId());
        if (ticket == null) throw new BusinessException("车票不存在");

        // 2.5 发车前30分钟禁止退票
        if (ticket.getDepartureTime() != null) {
            if (LocalDateTime.now().plusMinutes(30).isAfter(ticket.getDepartureTime())) {
                throw new BusinessException("发车前30分钟内不可退票");
            }
        }

        // 3. 恢复库存
        int rows = inventoryMapper.restoreStock(
                ticket.getTrainId(),
                ticket.getDepartureTime(),
                ticket.getSeatType()
        );
        if (rows == 0) {
            throw new BusinessException("恢复库存失败，请检查库存记录");
        }

        // 4. 创建退票记录
        RefundInfo refundInfo = new RefundInfo();
        refundInfo.setSaleId(saleInfo.getSaleId());
        refundInfo.setTicketId(saleInfo.getTicketId());
        refundInfo.setTrainId(saleInfo.getTrainId());
        refundInfo.setUserId(saleInfo.getUserId());
        refundInfo.setRefundTime(LocalDateTime.now());
        refundInfo.setRefundStatus("已完成");
        refundInfo.setRefundRemark("用户申请退票");
        refundInfo.setCreateTime(LocalDateTime.now());
        this.baseMapper.insertRefund(refundInfo); // 假设有这个自定义方法

        // 5. 更新销售记录状态为“已退票”
        saleInfoMapper.updateSaleStatus(saleInfo.getSaleId(), "已退票");

        // 6. 更新车票状态为“可售”
        ticketInfoMapper.updateTicketStatus(saleInfo.getTicketId(), "可售");

        return refundInfo;
    }
}