package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 车次发车时间表
 * 存储每个列车的具体发车时间(年月日时分)
 * 注意：direction字段已废弃，往返通过route_id的最后一位判断
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
     * 车次编号(冗余字段,方便查询，关联train_info.train_number)
     */
    @TableField("train_number")
    private String trainNumber;

    /**
     * 发车时间(具体的年月日时分)
     */
    @TableField("departure_time")
    private LocalDateTime departureTime;

    /**
     * 路线ID（关联router表）
     * 通过route_id的最后一位判断方向：0=往程，1=返程
     */
    @TableField("router_id")
    private Long routerId;
}
