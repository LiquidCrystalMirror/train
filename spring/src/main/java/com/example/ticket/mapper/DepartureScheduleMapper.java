package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.DepartureSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 车次发车时间Mapper
 */
@Mapper
public interface DepartureScheduleMapper extends BaseMapper<DepartureSchedule> {
    
    /**
     * 根据列车ID查询所有发车时间
     */
    List<DepartureSchedule> selectByTrainId(@Param("trainId") Integer trainId);
    
    /**
     * 根据发车时间范围查询车次
     */
    List<DepartureSchedule> selectByDepartureTimeRange(@Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 检查指定时间段是否有冲突的车次
     */
    List<DepartureSchedule> selectConflictSchedules(@Param("trainId") Integer trainId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);
}
