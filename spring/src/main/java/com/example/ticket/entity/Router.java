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

    @TableId(value = "router_id", type = IdType.AUTO)
    private Integer routerId;

    @TableField("router_name")
    private String routerName;

    private LocalDateTime createTime;

    // Router.java 增加
    @TableField("total_duration")
    private Double totalDuration;
}