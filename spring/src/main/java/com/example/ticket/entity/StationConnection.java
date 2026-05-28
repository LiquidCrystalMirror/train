package com.example.ticket.entity;



import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("station_connection")
public class StationConnection {

    // 联合主键：两个站点ID，且强制 stationAId < stationBId
    @TableId(value = "station_a_id")
    private Integer stationAId;

    private Integer stationBId;

    @TableField("travel_time_minutes")
    private Double travelTimeMinutes;

    private LocalDateTime createTime;
}