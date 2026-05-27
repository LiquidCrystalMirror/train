package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 车厢信息模板表
 * 作为车票生成的模板,管理员输入列车和时间后可快速生成一批次待售票
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("carriage_info")
public class CarriageInfo {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 车厢号(如1车、二等座01车)
     */
    @TableField("carriage_number")
    private String carriageNumber;

    /**
     * 座位号(如A1、05号)
     */
    @TableField("seat_number")
    private String seatNumber;

    /**
     * 座位类型(硬座/软座/二等座等)
     */
    @TableField("seat_type")
    private String seatType;
}
