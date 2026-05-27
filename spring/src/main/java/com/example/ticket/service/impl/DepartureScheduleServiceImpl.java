package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.DepartureSchedule;
import com.example.ticket.mapper.DepartureScheduleMapper;
import com.example.ticket.service.DepartureScheduleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 车次发车时间服务实现类
 */
@Service
public class DepartureScheduleServiceImpl extends ServiceImpl<DepartureScheduleMapper, DepartureSchedule> 
        implements DepartureScheduleService {

    @Override
    public List<DepartureSchedule> getSchedulesByTrainId(Integer trainId) {
        return baseMapper.selectByTrainId(trainId);
    }

    @Override
    public List<DepartureSchedule> getSchedulesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return baseMapper.selectByDepartureTimeRange(startTime, endTime);
    }

    @Override
    public boolean hasConflict(Integer trainId, LocalDateTime startTime, LocalDateTime endTime) {
        List<DepartureSchedule> conflicts = baseMapper.selectConflictSchedules(trainId, startTime, endTime);
        return conflicts != null && !conflicts.isEmpty();
    }

    @Override
    public boolean createSchedule(DepartureSchedule schedule) {
        return this.save(schedule);
    }
}
