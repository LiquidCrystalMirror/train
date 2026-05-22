package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.RefundInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;

@Mapper
public interface RefundInfoMapper extends BaseMapper<RefundInfo> {
    
    /**
     * 插入退票记录
     */
    @Insert("INSERT INTO refund_info (sale_id, ticket_id, train_id, user_id, refund_time, refund_status, refund_remark, create_time) " +
            "VALUES (#{saleId}, #{ticketId}, #{trainId}, #{userId}, #{refundTime}, #{refundStatus}, #{refundRemark}, #{createTime})")
    int insertRefund(RefundInfo refundInfo);
}