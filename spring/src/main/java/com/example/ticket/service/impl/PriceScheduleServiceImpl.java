package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.PriceSchedule;
import com.example.ticket.mapper.PriceScheduleMapper;
import com.example.ticket.service.PriceScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PriceScheduleServiceImpl extends ServiceImpl<PriceScheduleMapper, PriceSchedule>
        implements PriceScheduleService {

    @Override
    public Double getPrice(Integer trainId, Integer stationCount) {
        return baseMapper.selectPriceByTrainAndStations(trainId, stationCount);
    }

    @Override
    public boolean setPrice(Integer trainId, Integer stationCount, Double price) {
        PriceSchedule ps = new PriceSchedule();
        ps.setTrainId(trainId);
        ps.setStationCount(stationCount);
        ps.setPrice(price);
        return this.saveOrUpdate(ps);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSetPrices(Integer trainId, List<PriceSchedule> priceList) {
        // 确保所有条目都绑定正确的 trainId
        priceList.forEach(ps -> ps.setTrainId(trainId));
        baseMapper.batchInsertOrUpdate(priceList);
        return true;
    }

    @Override
    public boolean isComplete(Integer trainId, Integer totalStationCount) {
        if (totalStationCount == null || totalStationCount <= 0) {
            return false;
        }
        List<PriceSchedule> prices = this.list(Wrappers.<PriceSchedule>lambdaQuery()
                .eq(PriceSchedule::getTrainId, trainId)
                .le(PriceSchedule::getStationCount, totalStationCount)
                .orderByAsc(PriceSchedule::getStationCount));
        
        if (prices.size() != totalStationCount) {
            return false;
        }
        
        for (int i = 0; i < prices.size(); i++) {
            if (!prices.get(i).getStationCount().equals(i + 1)) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public List<PriceSchedule> getByTrainId(Integer trainId) {
        return this.list(Wrappers.<PriceSchedule>lambdaQuery()
                .eq(PriceSchedule::getTrainId, trainId)
                .orderByAsc(PriceSchedule::getStationCount));
    }
}