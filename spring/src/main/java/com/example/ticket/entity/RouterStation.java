package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 路线站点关联表
 * 描述一条路线由哪些站点按什么顺序组成
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("router_station")
public class RouterStation {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 路线ID(对应train_info.router_id)
     */
    @TableField("router_id")
    private Integer routerId;

    /**
     * 站点序号(从1开始,表示在路线中的顺序)
     */
    @TableField("station_seq")
    private Integer stationSeq;

    /**
     * 站点ID
     */
    @TableField("station_id")
    private Integer stationId;

    /**
     * 停留分钟数(在该站点停留的时间)
     */
    @TableField("stay_minutes")
    private Integer stayMinutes;
}
