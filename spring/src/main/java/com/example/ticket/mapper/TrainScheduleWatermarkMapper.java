package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.TrainScheduleWatermark;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 车次时间水位表Mapper
 */
@Mapper
public interface TrainScheduleWatermarkMapper extends BaseMapper<TrainScheduleWatermark> {
    
    /**
     * 查询指定列车的最新水位记录
     */
    TrainScheduleWatermark selectLatestByTrainId(@Param("trainId") String trainId);
    
    /**
     * 检查指定时间段是否在水位危险时间内
     */
    List<TrainScheduleWatermark> selectInDangerZone(@Param("trainId") String trainId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
    
    /**
     * 更新或插入水位记录(只保留最新的)
     */
    int upsertWatermark(TrainScheduleWatermark watermark);
}
