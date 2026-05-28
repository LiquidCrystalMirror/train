package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.DepartureSchedule;
import com.example.ticket.entity.Router;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.entity.TrainScheduleWatermark;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.DepartureScheduleMapper;
import com.example.ticket.mapper.RouterMapper;
import com.example.ticket.mapper.TrainScheduleWatermarkMapper;
import com.example.ticket.service.DepartureScheduleService;
import com.example.ticket.service.TrainService;
import com.example.ticket.util.DepartureTimeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 车次发车时间服务实现类
 */
@Service
public class DepartureScheduleServiceImpl extends ServiceImpl<DepartureScheduleMapper, DepartureSchedule> 
        implements DepartureScheduleService {

    @Resource
    private TrainService trainService;
    
    @Resource
    private RouterMapper routerMapper;
    
    @Resource
    private TrainScheduleWatermarkMapper watermarkMapper;

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
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createScheduleWithValidation(Integer trainId, LocalDateTime departureTime, Long routerId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 验证列车是否存在
            TrainInfo train = trainService.getById(trainId);
            if (train == null) {
                result.put("success", false);
                result.put("message", "列车不存在");
                return result;
            }
            
            // 2. 验证路线ID是否有效
            if (routerId == null) {
                result.put("success", false);
                result.put("message", "路线ID不能为空");
                return result;
            }
            
            // 3. 获取路线信息
            Router router = routerMapper.selectById(routerId);
            if (router == null) {
                result.put("success", false);
                result.put("message", "路线不存在");
                return result;
            }
            
            // 4. 查询水位表获取最后一次记录
            String trainNumber = train.getTrainNumber();
            TrainScheduleWatermark lastWatermark = watermarkMapper.selectLatestByTrainId(trainNumber);
            
            // 5. 确定基准时间
            LocalDateTime baseTime = DepartureTimeValidator.getBaseTime(lastWatermark);
            
            // 6. 验证发车时间是否在基准时间往后24小时内
            if (!DepartureTimeValidator.isValidDepartureTime(departureTime, baseTime)) {
                result.put("success", false);
                result.put("message", DepartureTimeValidator.buildErrorMessage(
                    DepartureTimeValidator.ErrorType.TIME_OUT_OF_RANGE, baseTime));
                return result;
            }
            
            // 7. 验证方向交替规则
            if (lastWatermark != null && lastWatermark.getRouteId() != null) {
                if (!DepartureTimeValidator.isAlternatingDirection(
                        lastWatermark.getRouteId(), routerId)) {
                    result.put("success", false);
                    result.put("message", DepartureTimeValidator.buildErrorMessage(
                        DepartureTimeValidator.ErrorType.DIRECTION_NOT_ALTERNATING));
                    return result;
                }
            }
            
            // 9. 计算到达时间
            LocalDateTime arriveTime = DepartureTimeValidator.calculateArrivalTime(departureTime, router);
            
            // 10. 检查是否有时间冲突（使用到达时间作为结束时间）
            if (hasConflict(trainId, departureTime, arriveTime)) {
                result.put("success", false);
                result.put("message", "该时间段已有车次安排");
                return result;
            }
            
            // 11. 创建发车计划
            DepartureSchedule schedule = new DepartureSchedule();
            schedule.setTrainId(trainId);
            schedule.setTrainNumber(trainNumber);
            schedule.setDepartureTime(departureTime);
            schedule.setRouterId(routerId);
            
            boolean saved = this.save(schedule);
            if (!saved) {
                result.put("success", false);
                result.put("message", "创建发车计划失败");
                return result;
            }
            
            // 12. 更新水位表（存储到达时间）
            // 先删除旧记录，再插入新记录（确保只有一条）
            watermarkMapper.deleteByTrainId(trainNumber);
            
            TrainScheduleWatermark watermark = new TrainScheduleWatermark();
            watermark.setTrainId(trainNumber);
            watermark.setRouteId(routerId);
            watermark.setDepartTime(departureTime);
            watermark.setArriveTime(arriveTime);
            // updated_by 可以从当前登录用户获取，这里暂时设为null
            watermark.setUpdatedBy(null);
            
            watermarkMapper.insert(watermark);
            
            result.put("success", true);
            result.put("message", "创建成功");
            result.put("scheduleId", schedule.getId());
            result.put("arriveTime", arriveTime);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "系统错误：" + e.getMessage());
            throw new BusinessException("创建发车计划失败：" + e.getMessage());
        }
        
        return result;
    }
}
