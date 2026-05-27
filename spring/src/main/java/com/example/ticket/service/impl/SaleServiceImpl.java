package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.PriceSchedule;
import com.example.ticket.entity.SaleInfo;
import com.example.ticket.entity.TicketInfo;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.PriceScheduleMapper;
import com.example.ticket.mapper.SaleInfoMapper;
import com.example.ticket.mapper.TicketInfoMapper;
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
    private PriceScheduleService priceScheduleService;
    
    /**
     * 售票业务方法（带事务）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer sellTicket(SaleInfo saleInfo, String userId) {
        // 1. 验证必填字段
        if (saleInfo.getTicketId() == null) {
            throw new BusinessException("车票ID不能为空");
        }
        if (saleInfo.getTrainId() == null) {
            throw new BusinessException("车次ID不能为空");
        }
        if (saleInfo.getStartStationSeq() == null || saleInfo.getEndStationSeq() == null) {
            throw new BusinessException("上车站点序号和下车站点序号不能为空");
        }
        
        // 2. 验证站点序号合法性
        if (saleInfo.getStartStationSeq() >= saleInfo.getEndStationSeq()) {
            throw new BusinessException("上车站点序号必须小于下车站点序号");
        }
        
        // 3. 查询车票信息，验证车票状态
        TicketInfo ticket = ticketInfoMapper.selectById(saleInfo.getTicketId());
        if (ticket == null) {
            throw new BusinessException(404, "车票不存在");
        }
        if (!"可售".equals(ticket.getTicketStatus())) {
            throw new BusinessException("车票状态不可售，当前状态：" + ticket.getTicketStatus());
        }
        
        // 4. 计算票价
        int stationCount = saleInfo.getEndStationSeq() - saleInfo.getStartStationSeq() + 1;
        Double price = priceScheduleService.getPriceByTrainAndStations(saleInfo.getTrainId(), stationCount);
        if (price == null) {
            throw new BusinessException("未找到该列车的价格策略，站点数：" + stationCount);
        }
        saleInfo.setPrice(price);
        
        // 5. 设置用户ID和时间
        saleInfo.setUserId(userId);
        saleInfo.setSaleTime(LocalDateTime.now());
        saleInfo.setSaleStatus("已出票");
        saleInfo.setCreateTime(LocalDateTime.now());
        
        // 6. 保存售票记录
        boolean save = this.save(saleInfo);
        if (!save) {
            throw new BusinessException("售票失败");
        }
        
        // 7. 售票成功后，把车票状态改为已售
        int updateResult = ticketInfoMapper.updateTicketStatus(saleInfo.getTicketId(), "已售");
        if (updateResult <= 0) {
            throw new BusinessException("更新车票状态失败");
        }
        
        return saleInfo.getSaleId();
    }
}