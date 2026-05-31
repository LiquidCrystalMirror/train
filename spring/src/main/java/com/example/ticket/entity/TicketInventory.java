// TicketInventory.java
package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ticket_inventory")
public class TicketInventory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Integer trainId;
    private LocalDateTime departureTime;
    private Long seatType;       // 座位类型编码（1=二等座, 2=一等座, 3=商务座）
    private Integer totalCount;
    private Integer soldCount;
    private Integer remainingCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}