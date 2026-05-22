package com.example.ticket.controller;

import com.example.ticket.entity.SaleInfo;
import com.example.ticket.entity.TicketInfo;
import com.example.ticket.entity.User;
import com.example.ticket.mapper.SaleInfoMapper;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.service.SaleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sale")
public class SaleController {

    @Resource
    private SaleService saleService;

    @Resource
    private TicketInfoMapper ticketInfoMapper;
    
    @Resource
    private SaleInfoMapper saleInfoMapper;

    @PostMapping("/do")
    public Map<String, Object> sell(@RequestBody SaleInfo saleInfo, HttpServletRequest request) {
        User login = (User) request.getAttribute("auth");
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 验证必填字段
            if (saleInfo.getTicketId() == null) {
                result.put("code", 400);
                result.put("msg", "车票ID不能为空");
                return result;
            }
            
            if (saleInfo.getTrainId() == null) {
                result.put("code", 400);
                result.put("msg", "车次ID不能为空");
                return result;
            }
            
            if (saleInfo.getStartStationSeq() == null || saleInfo.getEndStationSeq() == null) {
                result.put("code", 400);
                result.put("msg", "上车站点序号和下车站点序号不能为空");
                return result;
            }
            
            // 2. 验证站点序号合法性
            if (saleInfo.getStartStationSeq() >= saleInfo.getEndStationSeq()) {
                result.put("code", 400);
                result.put("msg", "上车站点序号必须小于下车站点序号");
                return result;
            }
            
            // 3. 查询车票信息，验证车票状态
            TicketInfo ticket = ticketInfoMapper.selectById(saleInfo.getTicketId());
            if (ticket == null) {
                result.put("code", 404);
                result.put("msg", "车票不存在");
                return result;
            }
            
            if (!"可售".equals(ticket.getTicketStatus())) {
                result.put("code", 400);
                result.put("msg", "车票状态不可售，当前状态：" + ticket.getTicketStatus());
                return result;
            }
            
            // 4. 设置用户ID和时间
            saleInfo.setUserId(login.getUserId());
            saleInfo.setSaleTime(LocalDateTime.now());
            saleInfo.setSaleStatus("已出票");
            saleInfo.setCreateTime(LocalDateTime.now());
            
            // 5. 保存售票记录
            boolean save = saleService.save(saleInfo);

            if (save) {
                // 6. 售票成功后，把车票状态改为 已售
                ticketInfoMapper.updateTicketStatus(saleInfo.getTicketId(), "已售");

                result.put("code", 200);
                result.put("msg", "售票成功");
                result.put("saleId", saleInfo.getSaleId());
            } else {
                result.put("code", 500);
                result.put("msg", "售票失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "售票异常：" + e.getMessage());
        }
        return result;
    }
}