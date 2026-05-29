package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.example.ticket.service.PriceScheduleService;
import com.example.ticket.service.RouterStationService;
import com.example.ticket.service.TicketService;
import com.example.ticket.service.TrainService;
import com.example.ticket.util.DepartureTimeValidator;
import com.example.ticket.vo.TrainScheduleQueryVO;
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
    private PriceScheduleService priceScheduleService;

    @Resource
    private TrainService trainService;
    
    @Resource
    private RouterMapper routerMapper;
    
    @Resource
    private TrainScheduleWatermarkMapper watermarkMapper;

    @Resource
    private RouterStationService routerStationService;

    @Resource
    private TicketService ticketService;

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

            // 3.1 获取路线的总站点数
            List<com.example.ticket.entity.RouterStation> stations = routerStationService.getStationsByRouterId(routerId);
            if (stations == null || stations.isEmpty()) {
                result.put("success", false);
                result.put("message", "该路线没有配置站点");
                return result;
            }
            int totalStations = stations.size();

            // 3.2 价格梯度完整性校验
            boolean priceComplete = priceScheduleService.isComplete(trainId, totalStations);
            if (!priceComplete) {
                result.put("success", false);
                result.put("message", String.format(
                        "价格梯度不完整，当前车次需配置 1~%d 站的价格才能发布班次", totalStations));
                return result;
            }


            // 4. 查询水位表获取最后一次记录
            TrainScheduleWatermark lastWatermark = watermarkMapper.selectLatestByTrainId(String.valueOf(trainId));

            // 5. 验证发车时间是否在基准时间往后24小时内（此方法内部同时检查 >= 基准时间 和 <= 基准时间+24h）
            if (!DepartureTimeValidator.isValidDepartureTime(departureTime, lastWatermark)) {
                LocalDateTime baseTime = DepartureTimeValidator.getBaseTime(lastWatermark);
                result.put("success", false);
                result.put("message", "发车时间必须在基准时间（" + baseTime + "）往后24小时内");
                return result;
            }
            
            // 8. 验证方向交替规则
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
            schedule.setTrainNumber(train.getTrainNumber());
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
            watermarkMapper.deleteByTrainId(String.valueOf(trainId));
            
            TrainScheduleWatermark watermark = new TrainScheduleWatermark();
            watermark.setTrainId(String.valueOf(trainId));
            watermark.setRouteId(routerId);
            watermark.setDepartTime(departureTime);
            watermark.setArriveTime(arriveTime);
            // updated_by 可以从当前登录用户获取，这里暂时设为null
            watermark.setUpdatedBy(null);
            
            watermarkMapper.insert(watermark);
            
            // 13. 自动生成车票并初始化库存（基于车厢模板）
            ticketService.generateTicketsWithoutWatermarkCheck(trainId, departureTime);
            
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


    @Override
    public Page<TrainScheduleQueryVO> querySchedulesByStations(Integer startStationId, Integer endStationId,
                                                               LocalDateTime startTime, Integer pageNum, Integer pageSize) {
        if (startStationId == null || endStationId == null || startTime == null) {
            throw new BusinessException("起点站、终点站和起始时间不能为空");
        }
        if (pageNum == null || pageNum < 1) pageNum = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;
        Page<TrainScheduleQueryVO> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectSchedulesByStartEndStationAndTime(page, startStationId, endStationId, startTime);
    }
}
