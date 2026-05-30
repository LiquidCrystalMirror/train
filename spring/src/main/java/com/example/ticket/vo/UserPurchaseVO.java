package com.example.ticket.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户购票记录聚合VO — JOIN sale_info + ticket_info + train_info + router_station + station
 * 包含站点名称、计算后的各站到达时间
 */
@Data
public class UserPurchaseVO {
    // --- sale_info ---
    private Integer saleId;
    private Integer ticketId;
    private Integer trainId;
    private String  userId;
    private Double  price;
    private LocalDateTime saleTime;
    private String  saleStatus;
    private Integer startStationSeq;
    private Integer endStationSeq;

    // --- ticket_info ---
    private String  carriageNumber;
    private String  seatNumber;
    private Long    seatType;
    private String  ticketStatus;
    private LocalDateTime departureTime;       // 始发站发车时间

    // --- train_info ---
    private String  trainNumber;

    // --- router_station（上车点） ---
    private Integer startStationId;
    private String  startStationName;
    private Double  startTimePrefixSum;

    // --- router_station（下车点） ---
    private Integer endStationId;
    private String  endStationName;
    private Double  endTimePrefixSum;

    // --- 计算后时间 ---
    private LocalDateTime startArrivalTime;     // 上车点到达时间（departureTime + startTimePrefixSum）
    private LocalDateTime endArrivalTime;       // 下车点到达时间（departureTime + endTimePrefixSum）
}
