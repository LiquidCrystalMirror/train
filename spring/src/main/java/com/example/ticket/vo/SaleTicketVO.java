package com.example.ticket.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 已售票聚合VO — JOIN sale_info + ticket_info + train_info
 */
@Data
public class SaleTicketVO {
    // --- sale_info ---
    private Integer saleId;
    private String  userId;          // 购买用户ID
    private Double  price;           // 票价
    private LocalDateTime saleTime;  // 购票时间
    private String  saleStatus;      // 已出票/已退票
    private Integer startStationSeq;
    private Integer endStationSeq;

    // --- ticket_info ---
    private Integer ticketId;
    private Integer trainId;
    private String  carriageNumber;  // 车厢号
    private String  seatNumber;      // 座位号
    private Long    seatType;        // 座位类型编码
    private String  ticketStatus;    // 车票状态
    private LocalDateTime departureTime; // 发车时间

    // --- train_info ---
    private String  trainNumber;     // 车次号
}
