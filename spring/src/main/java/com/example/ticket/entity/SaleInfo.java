package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sale_info")
public class SaleInfo {
    @TableId(type = IdType.AUTO)
    private Integer saleId;        // 售票记录ID
    
    private Integer ticketId;      // 车票ID（按类型购票时可为空，后端自动随机分配）
    
    private Integer trainId;       // 车次ID

    /** 座位类型编码（0=二等座/1=一等座/2=商务座），仅请求参数不存库 */
    @TableField(exist = false)
    private Long seatType;

    /** 发车时间，仅请求参数不存库 */
    @TableField(exist = false)
    private LocalDateTime departureTime;
    
    private String userId;         // 用户ID（字符串类型）
    
    private Integer startStationSeq; // 上车点对应的站点序号
    
    private Integer endStationSeq;   // 下车点对应的站点序号
    
    private LocalDateTime saleTime;// 购票时间
    
    private String saleStatus;     // 状态：已出票/已退票
    
    private LocalDateTime createTime; // 创建时间
    
    /**
     * 票价
     */
    private Double price;
}