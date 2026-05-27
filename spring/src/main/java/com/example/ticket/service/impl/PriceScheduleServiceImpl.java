package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.PriceSchedule;
import com.example.ticket.mapper.PriceScheduleMapper;
import com.example.ticket.service.PriceScheduleService;
import org.springframework.stereotype.Service;

/**
 * 价格策略服务实现类
 */
@Service
public class PriceScheduleServiceImpl extends ServiceImpl<PriceScheduleMapper, PriceSchedule> 
        implements PriceScheduleService {

    @Override
    public Double getPriceByTrainAndStations(Integer trainId, Integer stationCount) {
        return baseMapper.selectPriceByTrainAndStations(trainId, stationCount);
    }

    @Override
    public boolean setPrice(Integer trainId, Integer stationCount, Double price) {
        PriceSchedule priceSchedule = new PriceSchedule();
        priceSchedule.setTrainId(trainId);
        priceSchedule.setStationCount(stationCount);
        priceSchedule.setPrice(price);
        
        return this.saveOrUpdate(priceSchedule);
    }
}
