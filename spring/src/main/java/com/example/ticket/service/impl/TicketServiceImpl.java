package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.CarriageInfo;
import com.example.ticket.entity.TicketInfo;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.service.CarriageInfoService;
import com.example.ticket.service.TicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketInfoMapper, TicketInfo> implements TicketService {

    @Resource
    private CarriageInfoService carriageInfoService;

    @Override
    @Transactional
    public List<TicketInfo> generateTicketsFromTemplate(Integer trainId, LocalDateTime departureTime) {
        // 获取所有车厢模板
        List<CarriageInfo> templates = carriageInfoService.getAllTemplates();
        
        if (templates == null || templates.isEmpty()) {
            throw new RuntimeException("没有可用的车厢模板");
        }
        
        // 检查是否已经生成过该车次的车票
        LambdaQueryWrapper<TicketInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TicketInfo::getTrainId, trainId)
               .eq(TicketInfo::getDepartureTime, departureTime);
        long count = baseMapper.selectCount(wrapper);
        
        if (count > 0) {
            throw new RuntimeException("该车次的车票已生成");
        }
        
        // 根据模板批量生成车票
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
        
        // 批量保存
        this.saveBatch(tickets);
        
        return tickets;
    }

    @Override
    public List<TicketInfo> getAvailableTickets(Integer trainId) {
        LambdaQueryWrapper<TicketInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TicketInfo::getTrainId, trainId)
               .eq(TicketInfo::getTicketStatus, "可售")
               .orderByAsc(TicketInfo::getCarriageNumber, TicketInfo::getSeatNumber);
        return baseMapper.selectList(wrapper);
    }
}