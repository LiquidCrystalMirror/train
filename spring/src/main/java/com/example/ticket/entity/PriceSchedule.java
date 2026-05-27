package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 价格策略表
 * 根据列车和站点数量计算票价
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("price_schedule")
public class PriceSchedule {

    /**
     * 列车ID(关联train_info.train_id)
     */
    @TableId(value = "train_id")
    private Integer trainId;

    /**
     * 站点数量(经过的站点数)
     */
    @TableField("station_count")
    private Integer stationCount;

    /**
     * 票价
     */
    @TableField("price")
    private Double price;
}
