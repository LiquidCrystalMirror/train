package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sale_info")
public class SaleInfo {
    @TableId(type = IdType.AUTO)
    private Integer saleId;        // 售票记录ID
    private Integer ticketId;      // 车票ID
    private Integer trainId;       // 车次ID
    private String userId;         // 用户ID（字符串类型）
    private Integer startStationSeq; // 上车点对应的站点序号
    private Integer endStationSeq;   // 下车点对应的站点序号
    private LocalDateTime saleTime;// 购票时间
    private String saleStatus;     // 状态：已出票/已退票
    private LocalDateTime createTime; // 创建时间
}