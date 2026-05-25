package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.RefundInfo;

public interface RefundService extends IService<RefundInfo> {
    
    /**
     * 退票业务方法
     * @param saleId 售票记录ID
     * @return 退票记录
     */
    RefundInfo refundTicket(Integer saleId);
}