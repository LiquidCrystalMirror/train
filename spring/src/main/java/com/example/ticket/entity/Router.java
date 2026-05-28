package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("router")
public class Router {

    @TableId(value = "router_id", type = IdType.INPUT)
    private Long routerId;

    @TableField("router_name")
    private String routerName;

    @TableField("total_duration")
    private Double totalDuration;

    @TableField(value = "create_time", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
}