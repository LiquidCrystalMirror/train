package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.TicketInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TicketService extends IService<TicketInfo> {
    
    /**
     * 根据车厢模板批量生成车票
     * @param trainId 列车ID
     * @param departureTime 发车时间
     * @return 生成的车票列表
     */
    List<TicketInfo> generateTicketsFromTemplate(Integer trainId, LocalDateTime departureTime);
    
    /**
     * 查询指定车次的可售车票
     */
    List<TicketInfo> getAvailableTickets(Integer trainId);

    /**
     * 根据车厢模板批量生成车票（无水位检查，无水位表更新）
     * 前提：外部已确保发车时间合法，且同一车次+发车时间未生成过车票
     * @param trainId       列车ID
     * @param departureTime 发车时间
     * @return 生成的车票列表
     */
    List<TicketInfo> generateTicketsWithoutWatermarkCheck(Integer trainId, LocalDateTime departureTime);

}