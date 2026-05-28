package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.DepartureSchedule;
import com.example.ticket.vo.TrainScheduleQueryVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
     * 创建发车时间表（带水位表验证和方向交替检查）
     * @param trainId 列车ID
     * @param departureTime 发车时间
     * @param routerId 路线ID
     * @return 创建结果，包含成功/失败信息和错误消息
     */
    Map<String, Object> createScheduleWithValidation(Integer trainId, LocalDateTime departureTime, Long routerId);


    // 查询车次
    Page<TrainScheduleQueryVO> querySchedulesByStations(Integer startStationId, Integer endStationId,
                                                        LocalDateTime startTime, Integer pageNum, Integer pageSize);
}
