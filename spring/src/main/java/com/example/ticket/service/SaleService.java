package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.SaleInfo;

public interface SaleService extends IService<SaleInfo> {
    
    /**
     * 售票业务方法
     * @param saleInfo 售票信息
     * @param userId 用户ID（字符串类型）
     * @return 售票记录ID
     */
    Integer sellTicket(SaleInfo saleInfo, String userId);
    
    /**
     * 计算票价
     * @param trainId 车次ID
     * @param ticketId 车票ID
     * @param startStationSeq 上车站点序号
     * @param endStationSeq 下车站点序号
     * @return 计算后的票价
     */
    Double calculatePrice(Integer trainId, Integer ticketId, Integer startStationSeq, Integer endStationSeq);
}