package com.example.ticket.controller;

import com.example.ticket.entity.SaleInfo;
import com.example.ticket.entity.User;
import com.example.ticket.service.SaleService;
import com.example.ticket.util.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sale")
public class SaleController {

    @Resource
    private SaleService saleService;

    /**
     * 售票接口
     */
    @PostMapping("/do")
    public ApiResult<Integer> sell(@RequestBody SaleInfo saleInfo, HttpServletRequest request) {
        User login = (User) request.getAttribute("auth");
        
        // 调用Service层业务方法，异常由全局处理器处理
        Integer saleId = saleService.sellTicket(saleInfo, login.getUserId());
        
        return ApiResult.success("售票成功", saleId);
    }

    /**
     * 计算票价接口（售前预估，按座位类型计价，无需 ticketId）
     * seatType: 1=二等座, 2=一等座, 3=商务座
     */
    @PostMapping("/calculate-price")
    public ApiResult<Double> calculatePrice(@RequestBody Map<String, Object> params) {
        Integer trainId = params.get("trainId") != null ?
                ((Number) params.get("trainId")).intValue() : null;
        Long seatType = params.get("seatType") != null ?
                ((Number) params.get("seatType")).longValue() : null;
        Integer startStationSeq = params.get("startStationSeq") != null ?
                ((Number) params.get("startStationSeq")).intValue() : null;
        Integer endStationSeq = params.get("endStationSeq") != null ?
                ((Number) params.get("endStationSeq")).intValue() : null;

        Double price = saleService.calculatePrice(trainId, seatType, startStationSeq, endStationSeq);
        return ApiResult.success("计算成功", price);
    }
}
