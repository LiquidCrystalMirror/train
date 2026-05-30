package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.PriceSchedule;
import com.example.ticket.entity.SaleInfo;
import com.example.ticket.entity.TicketInfo;
import com.example.ticket.entity.TicketInventory;
import com.example.ticket.enums.SeatTypeEnum;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.PriceScheduleMapper;
import com.example.ticket.mapper.SaleInfoMapper;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.mapper.TicketInventoryMapper;
import com.example.ticket.service.PriceScheduleService;
import com.example.ticket.service.SaleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class SaleServiceImpl extends ServiceImpl<SaleInfoMapper, SaleInfo> implements SaleService {
    
    @Resource
    private TicketInfoMapper ticketInfoMapper;

    @Resource
    private TicketInventoryMapper inventoryMapper;

    @Resource
    private PriceScheduleService priceScheduleService;
    
    /**
     * 售票业务方法（带事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer sellTicket(SaleInfo saleInfo, String userId) {
        // 1. 参数校验
        if (saleInfo.getStartStationSeq() == null || saleInfo.getEndStationSeq() == null) {
            throw new BusinessException("上下车站点序号不能为空");
        }
        if (saleInfo.getStartStationSeq() >= saleInfo.getEndStationSeq()) {
            throw new BusinessException("上车站点序号必须小于下车站点序号");
        }

        // 2. 确定车票：支持两种模式
        TicketInfo ticket;
        if (saleInfo.getTicketId() != null) {
            // 模式一：直接指定 ticketId（管理员/旧流程）
            ticket = ticketInfoMapper.selectById(saleInfo.getTicketId());
            if (ticket == null) throw new BusinessException(404, "车票不存在");
        } else if (saleInfo.getSeatType() != null && saleInfo.getTrainId() != null
                && saleInfo.getDepartureTime() != null) {
            // 模式二：按座位类型随机选一张可售票（用户端）
            ticket = ticketInfoMapper.selectOneAvailableRandom(
                    saleInfo.getTrainId(), saleInfo.getDepartureTime(), saleInfo.getSeatType());
            if (ticket == null) throw new BusinessException("该类型车票已售罄");
            saleInfo.setTicketId(ticket.getTicketId());
        } else {
            throw new BusinessException("请指定车票ID，或选择座位类型购票");
        }

        if (!"可售".equals(ticket.getTicketStatus())) {
            throw new BusinessException("车票状态不可售，当前状态：" + ticket.getTicketStatus());
        }

        // 3. 查询库存记录（用于乐观锁版本号）
        TicketInventory inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<TicketInventory>()
                        .eq(TicketInventory::getTrainId, ticket.getTrainId())
                        .eq(TicketInventory::getDepartureTime, ticket.getDepartureTime())
                        .eq(TicketInventory::getSeatType, ticket.getSeatType())
        );
        if (inv == null || inv.getRemainingCount() <= 0) {
            throw new BusinessException("库存不足或未初始化");
        }

        // 4. 乐观锁扣减库存（使用剩余数量作为乐观锁条件）
        int rows = inventoryMapper.deductStock(
                ticket.getTrainId(),
                ticket.getDepartureTime(),
                ticket.getSeatType(),
                inv.getRemainingCount()
        );
        if (rows == 0) {
            throw new BusinessException("购票失败，库存已被扣减，请重试");
        }

        // 5. 计算票价
        int stationCount = saleInfo.getEndStationSeq() - saleInfo.getStartStationSeq() + 1;
        Double basePrice = priceScheduleService.getPrice(saleInfo.getTrainId(), stationCount);
        if (basePrice == null) {
            throw new BusinessException("未找到该列车的价格策略，站点数：" + stationCount);
        }
        
        // 根据座位类型应用价格系数
        SeatTypeEnum seatType = SeatTypeEnum.fromCode(ticket.getSeatType());
        if (seatType == null) {
            throw new BusinessException("未知的座位类型：" + ticket.getSeatType());
        }
        double finalPrice = basePrice * seatType.getPriceMultiplier();
        saleInfo.setPrice(finalPrice);
        saleInfo.setUserId(userId);
        saleInfo.setSaleTime(LocalDateTime.now());
        saleInfo.setSaleStatus("已出票");
        saleInfo.setCreateTime(LocalDateTime.now());

        // 6. 保存销售记录
        boolean saved = this.save(saleInfo);
        if (!saved) throw new BusinessException("售票失败");

        // 7. 更新车票状态为“已售”
        int updateResult = ticketInfoMapper.updateTicketStatus(saleInfo.getTicketId(), "已售");
        if (updateResult <= 0) throw new BusinessException("更新车票状态失败");

        return saleInfo.getSaleId();
    }

    @Override
    public Double calculatePrice(Integer trainId, Integer ticketId, Integer startStationSeq, Integer endStationSeq) {
        if (trainId == null || ticketId == null || startStationSeq == null || endStationSeq == null) {
            throw new BusinessException("参数不能为空");
        }
        if (startStationSeq >= endStationSeq) {
            throw new BusinessException("上车站点序号必须小于下车站点序号");
        }

        TicketInfo ticket = ticketInfoMapper.selectById(ticketId);
        if (ticket == null) {
            throw new BusinessException("车票不存在");
        }

        int stationCount = endStationSeq - startStationSeq + 1;
        Double basePrice = priceScheduleService.getPrice(trainId, stationCount);
        if (basePrice == null) {
            throw new BusinessException("未找到该列车的价格策略，站点数：" + stationCount);
        }

        SeatTypeEnum seatType = SeatTypeEnum.fromCode(ticket.getSeatType());
        if (seatType == null) {
            throw new BusinessException("未知的座位类型：" + ticket.getSeatType());
        }

        return basePrice * seatType.getPriceMultiplier();
    }
}