package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.PriceSchedule;

import java.util.List;

/**
 * 价格策略服务接口
 */
public interface PriceScheduleService extends IService<PriceSchedule> {
    Double getPrice(Integer trainId, Integer stationCount);
    boolean setPrice(Integer trainId, Integer stationCount, Double price);
    boolean batchSetPrices(Integer trainId, List<PriceSchedule> priceList);
    boolean isComplete(Integer trainId, Integer totalStationCount);  // 完整性检查
    List<PriceSchedule> getByTrainId(Integer trainId);
}
