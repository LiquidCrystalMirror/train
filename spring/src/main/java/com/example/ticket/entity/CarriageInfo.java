package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("carriage_info")
public class CarriageInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("carriage_number")
    private String carriageNumber;

    @TableField("seat_number")
    private String seatNumber;

    @TableField("seat_type")
    private Long seatType;  // 改为 Long，存储枚举 code
}