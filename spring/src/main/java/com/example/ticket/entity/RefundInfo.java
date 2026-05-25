package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("refund_info")
public class RefundInfo {
    @TableId(type = IdType.AUTO)
    private Integer refundId;     // 退票ID
    private Integer saleId;       // 售票记录ID
    private Integer ticketId;     // 车票ID
    private Integer trainId;      // 车次ID
    private String userId;        // 用户ID（字符串类型）
    private LocalDateTime refundTime; // 退票时间
    private String refundStatus;  // 退票状态
    private String refundRemark;  // 退票备注
    private LocalDateTime createTime; // 创建时间
}