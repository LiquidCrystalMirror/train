package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.RefundInfo;
import com.example.ticket.vo.UserRefundVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RefundInfoMapper extends BaseMapper<RefundInfo> {
    
    /**
     * 插入退票记录
     */
    @Insert("INSERT INTO refund_info (sale_id, ticket_id, train_id, user_id, refund_time, refund_status, refund_remark, create_time) " +
            "VALUES (#{saleId}, #{ticketId}, #{trainId}, #{userId}, #{refundTime}, #{refundStatus}, #{refundRemark}, #{createTime})")
    int insertRefund(RefundInfo refundInfo);

    /**
     * 分页查询用户退票记录（含站点名称和timePrefixSum）
     */
    @Select("SELECT r.refund_id AS refundId, r.sale_id AS saleId, r.ticket_id AS ticketId, " +
            "r.train_id AS trainId, r.user_id AS userId, r.refund_time AS refundTime, " +
            "r.refund_status AS refundStatus, r.refund_remark AS refundRemark, " +
            "s.price AS refundAmount, s.start_station_seq AS startStationSeq, s.end_station_seq AS endStationSeq, " +
            "t.carriage_number AS carriageNumber, t.seat_number AS seatNumber, " +
            "t.seat_type AS seatType, t.ticket_status AS ticketStatus, " +
            "t.departure_time AS departureTime, tr.train_number AS trainNumber, " +
            "rs1.station_id AS startStationId, st1.station_name AS startStationName, " +
            "rs1.time_prefix_sum AS startTimePrefixSum, " +
            "rs2.station_id AS endStationId, st2.station_name AS endStationName, " +
            "rs2.time_prefix_sum AS endTimePrefixSum " +
            "FROM refund_info r " +
            "LEFT JOIN sale_info s ON r.sale_id = s.sale_id " +
            "LEFT JOIN ticket_info t ON r.ticket_id = t.ticket_id " +
            "LEFT JOIN train_info tr ON r.train_id = tr.train_id " +
            "LEFT JOIN router_station rs1 ON tr.router_id = rs1.router_id AND s.start_station_seq = rs1.station_seq " +
            "LEFT JOIN station st1 ON rs1.station_id = st1.station_id " +
            "LEFT JOIN router_station rs2 ON tr.router_id = rs2.router_id AND s.end_station_seq = rs2.station_seq " +
            "LEFT JOIN station st2 ON rs2.station_id = st2.station_id " +
            "WHERE r.user_id = #{userId} " +
            "ORDER BY r.refund_time DESC")
    Page<UserRefundVO> selectUserRefundPage(Page<UserRefundVO> page, @Param("userId") String userId);
}