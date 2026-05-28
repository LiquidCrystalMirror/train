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
     * 路线ID(关联router表的router_id，最后一位表示方向：0=往程，1=返程)
     */
    private Long routerId;
    
    /**
     * 反向路线ID
     */
    private Long oppsiteRouterId;
}