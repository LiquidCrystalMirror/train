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
    private Long seatType;       // 座位类型编码（0/1/2）
    private Integer totalCount;
    private Integer soldCount;
    private Integer remainingCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}