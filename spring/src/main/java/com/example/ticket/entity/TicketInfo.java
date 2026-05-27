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
    
    /**
     * 关联车次ID
     */
    private Integer trainId;
    
    /**
     * 车厢号(如1车、二等座01车)
     */
    private String carriageNumber;
    
    /**
     * 座位号(如A1、05号)
     */
    private String seatNumber;
    
    /**
     * 座位类型(硬座/软座/二等座等)
     */
    private String seatType;
    
    /**
     * 车票状态(可售/已售/锁定)
     */
    private String ticketStatus;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 发车时间(该票对应的发车时间)
     */
    private LocalDateTime departureTime;
}