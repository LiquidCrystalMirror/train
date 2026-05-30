package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.SaleInfo;

public interface SaleService extends IService<SaleInfo> {
    
    /**
     * 售票业务方法（支持两种模式）
     * 1. 指定 ticketId 购票（管理员/旧流程）
     * 2. 按 seatType 随机选票购票（用户端新流程）
     * @param saleInfo 售票信息（ticketId或seatType二选一）
     * @param userId 用户ID
     * @return 售票记录ID
     */
    Integer sellTicket(SaleInfo saleInfo, String userId);
    
    /**
     * 计算票价（售前预估，无需 ticketId）
     * @param trainId 车次ID
     * @param seatType 座位类型编码（0=二等座, 1=一等座, 2=商务座）
     * @param startStationSeq 上车站点序号
     * @param endStationSeq 下车站点序号
     * @return 计算后的票价
     */
    Double calculatePrice(Integer trainId, Long seatType, Integer startStationSeq, Integer endStationSeq);
}