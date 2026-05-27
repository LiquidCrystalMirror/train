package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.PriceSchedule;

/**
 * 价格策略服务接口
 */
public interface PriceScheduleService extends IService<PriceSchedule> {
    
    /**
     * 根据列车ID和站点数量查询价格
     */
    Double getPriceByTrainAndStations(Integer trainId, Integer stationCount);
    
    /**
     * 设置价格策略
     */
    boolean setPrice(Integer trainId, Integer stationCount, Double price);
}
