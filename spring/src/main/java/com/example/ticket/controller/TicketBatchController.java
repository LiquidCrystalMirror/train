package com.example.ticket.controller;

import com.example.ticket.entity.TicketInfo;
import com.example.ticket.service.TicketService;
import com.example.ticket.service.impl.TicketServiceImpl;
import com.example.ticket.util.ApiResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 车票批量生成Controller
 */
@RestController
@RequestMapping("/api/v1/ticket/batch")
public class TicketBatchController {

    @Resource
    private TicketServiceImpl ticketService;

    /**
     * 批量生成车票（带水位表检查）
     */
    @PostMapping("/generate")
    public ApiResult<List<TicketInfo>> generateTickets(@RequestBody Map<String, Object> params) {
        Integer trainId = (Integer) params.get("trainId");
        String departureTimeStr = (String) params.get("departureTime");
        Integer adminId = (Integer) params.get("adminId");
        
        if (trainId == null || departureTimeStr == null) {
            return ApiResult.error(400, "参数不完整");
        }
        
        LocalDateTime departureTime = LocalDateTime.parse(departureTimeStr);
        
        try {
            List<TicketInfo> tickets = ticketService.generateTicketsFromTemplateWithWatermark(
                trainId, 
                departureTime, 
                adminId
            );
            return ApiResult.success("生成成功，共" + tickets.size() + "张车票", tickets);
        } catch (Exception e) {
            return ApiResult.error(400, e.getMessage());
        }
    }
}
