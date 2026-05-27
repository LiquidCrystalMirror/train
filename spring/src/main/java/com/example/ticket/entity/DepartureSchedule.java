package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 车次发车时间表
 * 存储每个列车的具体发车时间(年月日时)和运行方向
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("departure_schedule")
public class DepartureSchedule {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 列车ID(关联train_info.train_id)
     */
    @TableField("train_id")
    private Integer trainId;

    /**
     * 车次编号(冗余字段,方便查询)
     */
    @TableField("train_name")
    private String trainName;

    /**
     * 发车时间(具体的年月日时分)
     */
    @TableField("departure_time")
    private LocalDateTime departureTime;

    /**
     * 运行方向: 0-顺行(正序), 1-逆行(逆序)
     */
    @TableField("direction")
    private Integer direction;
}
