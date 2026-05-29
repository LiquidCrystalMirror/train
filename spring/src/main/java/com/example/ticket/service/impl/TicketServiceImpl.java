package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.CarriageInfo;
import com.example.ticket.entity.TicketInventory;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.entity.TicketInfo;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.mapper.TicketInventoryMapper;
import com.example.ticket.service.CarriageInfoService;
import com.example.ticket.service.TrainScheduleWatermarkService;
import com.example.ticket.service.TrainService;
import com.example.ticket.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketInfoMapper, TicketInfo> implements TicketService {

    @Resource
    private CarriageInfoService carriageInfoService;

    @Resource
    private TicketInventoryMapper inventoryMapper;

    @Resource
    private TrainService trainService;
    
    @Resource
    private TrainScheduleWatermarkService watermarkService;

    @Override
    @Transactional
    public List<TicketInfo> generateTicketsFromTemplate(Integer trainId, LocalDateTime departureTime) {
        return generateTicketsFromTemplateWithWatermark(trainId, departureTime, null);
    }
    
    /**
     * 根据车厢模板批量生成车票（带水位表检查）
     * @param trainId 列车ID
     * @param departureTime 发车时间
     * @param adminId 管理员ID（用于更新水位表）
     * @return 生成的车票列表
     */
    public List<TicketInfo> generateTicketsFromTemplateWithWatermark(
            Integer trainId, 
            LocalDateTime departureTime,
            Integer adminId
    ) {
        // 1. 获取列车信息
        TrainInfo train = trainService.getById(trainId);
        if (train == null) {
            throw new BusinessException("列车不存在");
        }
        
        // 2. 计算到达时间
        if (train.getTimeConsuming() == null) {
            throw new BusinessException("列车总耗时未设置");
        }
        LocalDateTime arriveTime = departureTime.plusMinutes(train.getTimeConsuming());
        
        // 3. 检查危险时间区域
        if (watermarkService.isInDangerZone(String.valueOf(trainId), departureTime, arriveTime)) {
            throw new BusinessException(
                "该时间段在危险区域内（已有车次运行），无法生成车票。" +
                "建议在水位时间之后" + getBufferMinutes() + "分钟后再出票"
            );
        }
        
        // 4. 获取所有车厢模板
        List<CarriageInfo> templates = carriageInfoService.getAllTemplates();
        
        if (templates == null || templates.isEmpty()) {
            throw new BusinessException("没有可用的车厢模板");
        }
        
        // 5. 检查是否已经生成过该车次的车票
        LambdaQueryWrapper<TicketInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TicketInfo::getTrainId, trainId)
               .eq(TicketInfo::getDepartureTime, departureTime);
        long count = baseMapper.selectCount(wrapper);
        
        if (count > 0) {
            throw new BusinessException("该车次的车票已生成");
        }
        
        // 6. 根据模板批量生成车票
        List<TicketInfo> tickets = new ArrayList<>();
        for (CarriageInfo template : templates) {
            TicketInfo ticket = new TicketInfo();
            ticket.setTrainId(trainId);
            ticket.setCarriageNumber(template.getCarriageNumber());
            ticket.setSeatNumber(template.getSeatNumber());
            ticket.setSeatType(template.getSeatType());
            ticket.setTicketStatus("可售");
            ticket.setDepartureTime(departureTime);
            ticket.setCreateTime(LocalDateTime.now());
            tickets.add(ticket);
        }
        
        // 7. 批量保存车票
        this.saveBatch(tickets);
        
        // 8. 更新水位表
        if (adminId != null && train.getRouterId() != null) {
            watermarkService.updateWatermark(
                String.valueOf(trainId),
                train.getRouterId(),
                departureTime,
                arriveTime,
                adminId
            );
        }
        
        return tickets;
    }
    
    /**
     * 获取缓冲时间（分钟）
     */
    private int getBufferMinutes() {
        return 30; // 默认30分钟
    }

    @Override
    public List<TicketInfo> getAvailableTickets(Integer trainId) {
        LambdaQueryWrapper<TicketInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TicketInfo::getTrainId, trainId)
               .eq(TicketInfo::getTicketStatus, "可售")
               .orderByAsc(TicketInfo::getCarriageNumber, TicketInfo::getSeatNumber);
        return baseMapper.selectList(wrapper);
    }

    // TicketServiceImpl.java 中的核心方法
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TicketInfo> generateTicketsWithoutWatermarkCheck(Integer trainId, LocalDateTime departureTime) {
        // 1. 校验列车
        TrainInfo train = trainService.getById(trainId);
        if (train == null) throw new BusinessException("列车不存在");

        // 2. 检查是否已生成过该车次的车票
        LambdaQueryWrapper<TicketInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TicketInfo::getTrainId, trainId)
                .eq(TicketInfo::getDepartureTime, departureTime);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("该车次的车票已生成");
        }

        // 3. 获取所有车厢模板
        List<CarriageInfo> templates = carriageInfoService.getAllTemplates();
        if (templates == null || templates.isEmpty()) {
            throw new BusinessException("没有可用的车厢模板");
        }

        // 4. 生成车票并统计各座位类型数量
        List<TicketInfo> tickets = new ArrayList<>();
        Map<Long, Integer> typeCountMap = new HashMap<>();  // seatType -> 数量

        for (CarriageInfo template : templates) {
            TicketInfo ticket = new TicketInfo();
            ticket.setTrainId(trainId);
            ticket.setCarriageNumber(template.getCarriageNumber());
            ticket.setSeatNumber(template.getSeatNumber());
            ticket.setSeatType(template.getSeatType());      // 直接使用 Long
            ticket.setTicketStatus("可售");
            ticket.setDepartureTime(departureTime);
            ticket.setCreateTime(LocalDateTime.now());
            tickets.add(ticket);

            Long seatType = template.getSeatType();
            typeCountMap.put(seatType, typeCountMap.getOrDefault(seatType, 0) + 1);
        }

        // 5. 批量保存车票
        this.saveBatch(tickets);

        // 6. 初始化或更新库存表
        for (Map.Entry<Long, Integer> entry : typeCountMap.entrySet()) {
            Long seatType = entry.getKey();
            Integer count = entry.getValue();

            // 查询是否已有库存记录
            TicketInventory existing = inventoryMapper.selectOne(
                    new LambdaQueryWrapper<TicketInventory>()
                            .eq(TicketInventory::getTrainId, trainId)
                            .eq(TicketInventory::getDepartureTime, departureTime)
                            .eq(TicketInventory::getSeatType, seatType)
            );

            if (existing == null) {
                TicketInventory inv = new TicketInventory();
                inv.setTrainId(trainId);
                inv.setDepartureTime(departureTime);
                inv.setSeatType(seatType);
                inv.setTotalCount(count);
                inv.setSoldCount(0);
                inv.setRemainingCount(count);
                inventoryMapper.insert(inv);
            } else {
                // 如果库存记录已存在（例如重复生成），则累加数量
                existing.setTotalCount(existing.getTotalCount() + count);
                existing.setRemainingCount(existing.getRemainingCount() + count);
                inventoryMapper.updateById(existing);
            }
        }

        return tickets;
    }
}