// PriceScheduleMapper.java
package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.PriceSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PriceScheduleMapper extends BaseMapper<PriceSchedule> {

    // 根据车次ID和站数查询价格
    @Select("SELECT price FROM price_schedule WHERE train_id = #{trainId} AND station_count = #{stationCount}")
    Double selectPriceByTrainAndStations(@Param("trainId") Integer trainId,
                                         @Param("stationCount") Integer stationCount);

    // 查询某车次已配置的不同站数数量（用于完整性校验）
    @Select("SELECT COUNT(DISTINCT station_count) FROM price_schedule WHERE train_id = #{trainId}")
    Integer countConfiguredStationCounts(@Param("trainId") Integer trainId);

    // 批量插入或更新（使用 ON DUPLICATE KEY UPDATE）
    void batchInsertOrUpdate(@Param("list") List<PriceSchedule> list);
}