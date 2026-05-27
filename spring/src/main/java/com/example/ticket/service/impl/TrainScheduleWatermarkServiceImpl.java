package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.TrainScheduleWatermark;
import com.example.ticket.mapper.TrainScheduleWatermarkMapper;
import com.example.ticket.service.TrainScheduleWatermarkService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 车次时间水位表服务实现类
 */
@Service
public class TrainScheduleWatermarkServiceImpl extends ServiceImpl<TrainScheduleWatermarkMapper, TrainScheduleWatermark> 
        implements TrainScheduleWatermarkService {

    /**
     * 危险时间缓冲(分钟) - 水位时间之后多久可以出票
     */
    private static final int DANGER_BUFFER_MINUTES = 30;

    @Override
    public TrainScheduleWatermark getLatestWatermark(String trainId) {
        return baseMapper.selectLatestByTrainId(trainId);
    }

    @Override
    public boolean isInDangerZone(String trainId, LocalDateTime startTime, LocalDateTime endTime) {
        List<TrainScheduleWatermark> dangerZones = baseMapper.selectInDangerZone(trainId, startTime, endTime);
        return dangerZones != null && !dangerZones.isEmpty();
    }

    @Override
    public boolean updateWatermark(String trainId, Integer routeId, LocalDateTime departTime, 
                                   LocalDateTime arriveTime, Integer updatedBy) {
        TrainScheduleWatermark watermark = new TrainScheduleWatermark();
        watermark.setTrainId(trainId);
        watermark.setRouteId(routeId);
        watermark.setDepartTime(departTime);
        watermark.setArriveTime(arriveTime);
        watermark.setUpdatedBy(updatedBy);
        
        return baseMapper.upsertWatermark(watermark) > 0;
    }

    @Override
    public LocalDateTime getEarliestTicketTime(String trainId) {
        TrainScheduleWatermark latest = getLatestWatermark(trainId);
        if (latest == null) {
            // 没有水位记录,可以立即出票
            return LocalDateTime.now();
        }
        
        // 返回水位到达时间 + 缓冲时间
        return latest.getArriveTime().plusMinutes(DANGER_BUFFER_MINUTES);
    }
}
