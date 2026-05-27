package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.DepartureSchedule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 车次发车时间服务接口
 */
public interface DepartureScheduleService extends IService<DepartureSchedule> {
    
    /**
     * 根据列车ID查询所有发车时间
     */
    List<DepartureSchedule> getSchedulesByTrainId(Integer trainId);
    
    /**
     * 根据发车时间范围查询车次
     */
    List<DepartureSchedule> getSchedulesByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 检查指定时间段是否有冲突的车次
     */
    boolean hasConflict(Integer trainId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 创建发车时间表
     */
    boolean createSchedule(DepartureSchedule schedule);
}
