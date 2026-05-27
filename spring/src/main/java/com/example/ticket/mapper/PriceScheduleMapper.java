package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.PriceSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 价格策略Mapper
 */
@Mapper
public interface PriceScheduleMapper extends BaseMapper<PriceSchedule> {
    
    /**
     * 根据列车ID和站点数量查询价格
     */
    Double selectPriceByTrainAndStations(@Param("trainId") Integer trainId,
                                         @Param("stationCount") Integer stationCount);
}
