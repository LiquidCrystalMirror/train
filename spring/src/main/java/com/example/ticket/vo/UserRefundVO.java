package com.example.ticket.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户退票记录聚合VO — JOIN refund_info + ticket_info + train_info + router_station + station
 */
@Data
public class UserRefundVO {
    // --- refund_info ---
    private Integer refundId;
    private Integer saleId;
    private Integer ticketId;
    private Integer trainId;
    private String  userId;
    private LocalDateTime refundTime;
    private String  refundStatus;
    private String  refundRemark;
    private Double  refundAmount;

    // --- ticket_info ---
    private String  carriageNumber;
    private String  seatNumber;
    private Long    seatType;
    private String  ticketStatus;
    private LocalDateTime departureTime;

    // --- train_info ---
    private String  trainNumber;

    // --- 站点信息 ---
    private Integer startStationId;
    private String  startStationName;
    private Double  startTimePrefixSum;

    private Integer endStationId;
    private String  endStationName;
    private Double  endTimePrefixSum;

    // --- 计算后时间 ---
    private LocalDateTime startArrivalTime;
    private LocalDateTime endArrivalTime;
}
