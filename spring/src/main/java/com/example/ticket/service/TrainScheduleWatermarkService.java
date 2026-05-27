package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.TrainScheduleWatermark;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 车次时间水位表服务接口
 */
public interface TrainScheduleWatermarkService extends IService<TrainScheduleWatermark> {
    
    /**
     * 查询指定列车的最新水位记录
     */
    TrainScheduleWatermark getLatestWatermark(String trainId);
    
    /**
     * 检查指定时间段是否在水位危险时间内
     */
    boolean isInDangerZone(String trainId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 更新水位记录
     */
    boolean updateWatermark(String trainId, Integer routeId, LocalDateTime departTime, 
                           LocalDateTime arriveTime, Integer updatedBy);
    
    /**
     * 获取可以出票的最早时间(水位时间之后的一段时间)
     */
    LocalDateTime getEarliestTicketTime(String trainId);
}
