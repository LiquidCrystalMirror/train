package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ticket_info")
public class TicketInfo {
    @TableId(type = IdType.AUTO)
    private Integer ticketId;
    private Integer trainId;
    private String carriageNumber;
    private String seatNumber;
    private String seatType;
    private String ticketStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private BigDecimal price;
}