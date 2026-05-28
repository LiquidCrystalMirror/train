package com.example.ticket.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TrainScheduleQueryVO {
    private Integer trainId;
    private String trainNumber;
    private LocalDateTime departureTime;      // 路线首站发车时间
    private LocalDateTime startArrivalTime;   // 到达起点站的时间
    private LocalDateTime endArrivalTime;     // 到达终点站的时间
    private Long routerId;
    private String routerName;
}