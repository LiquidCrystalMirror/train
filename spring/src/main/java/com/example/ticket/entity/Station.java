package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("station")
public class Station {

    @TableId(value = "station_id", type = IdType.AUTO)
    private Integer stationId;

    @TableField("station_name")
    private String stationName;

    private LocalDateTime createTime;
}