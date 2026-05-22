package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("train_info")
public class TrainInfo {
    @TableId(type = IdType.AUTO)
    private Integer trainId;
    private String trainNumber;
    private Integer totalStations;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String runTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}