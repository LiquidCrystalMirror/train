package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("train_info")
public class TrainInfo {
    @TableId(type = IdType.AUTO)
    private Integer trainId;
    
    /**
     * 车次编号(如G123)
     */
    private String trainNumber;
    
    /**
     * 总耗时(分钟)
     */
    private Integer timeConsuming;
    
    /**
     * 路线ID(关联router_station.router_id)
     */
    private Integer routerId;
}