package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 车次时间水位表
 * 用于防止同一列车在危险时间段内重复排班
 * 仅存储最新的车次数据,供管理者发行车票时使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("train_schedule_watermark")
public class TrainScheduleWatermark {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 车次号(对应train_info.train_number)
     */
    @TableField("train_id")
    private String trainId;

    /**
     * 路线ID（关联router表，最后一位表示方向：0=往程，1=返程）
     */
    @TableField("route_id")
    private Long routeId;

    /**
     * 始发时间
     */
    @TableField("depart_time")
    private LocalDateTime departTime;

    /**
     * 终到时间
     */
    @TableField("arrive_time")
    private LocalDateTime arriveTime;

    /**
     * 创建人ID(管理员ID)
     */
    @TableField("updated_by")
    private Integer updatedBy;
}
