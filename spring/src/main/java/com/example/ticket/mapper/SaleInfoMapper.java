package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.SaleInfo;
import com.example.ticket.vo.SaleTicketVO;
import com.example.ticket.vo.UserPurchaseVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SaleInfoMapper extends BaseMapper<SaleInfo> {
    
    /**
     * 根据销售ID查询销售记录
     */
    @Select("SELECT * FROM sale_info WHERE sale_id = #{saleId}")
    SaleInfo selectBySaleId(@Param("saleId") Integer saleId);
    
    /**
     * 更新销售记录状态
     */
    @Update("UPDATE sale_info SET sale_status = #{status} WHERE sale_id = #{saleId}")
    int updateSaleStatus(@Param("saleId") Integer saleId, @Param("status") String status);

    /**
     * 分页查询已售票（JOIN ticket_info + train_info）
     */
    @Select("SELECT s.sale_id AS saleId, s.user_id AS userId, s.price, s.sale_time AS saleTime, " +
            "s.sale_status AS saleStatus, s.start_station_seq AS startStationSeq, s.end_station_seq AS endStationSeq, " +
            "t.ticket_id AS ticketId, t.train_id AS trainId, t.carriage_number AS carriageNumber, " +
            "t.seat_number AS seatNumber, t.seat_type AS seatType, t.ticket_status AS ticketStatus, " +
            "t.departure_time AS departureTime, tr.train_number AS trainNumber " +
            "FROM sale_info s " +
            "LEFT JOIN ticket_info t ON s.ticket_id = t.ticket_id " +
            "LEFT JOIN train_info tr ON t.train_id = tr.train_id " +
            "ORDER BY s.sale_time DESC")
    Page<SaleTicketVO> selectSoldTicketPage(Page<SaleTicketVO> page);

    /**
     * 分页查询用户购票记录（含站点名称和timePrefixSum）
     */
    @Select("SELECT s.sale_id AS saleId, s.ticket_id AS ticketId, s.train_id AS trainId, " +
            "s.user_id AS userId, s.price, s.sale_time AS saleTime, s.sale_status AS saleStatus, " +
            "s.start_station_seq AS startStationSeq, s.end_station_seq AS endStationSeq, " +
            "t.carriage_number AS carriageNumber, t.seat_number AS seatNumber, " +
            "t.seat_type AS seatType, t.ticket_status AS ticketStatus, " +
            "t.departure_time AS departureTime, tr.train_number AS trainNumber, " +
            "rs1.station_id AS startStationId, st1.station_name AS startStationName, " +
            "rs1.time_prefix_sum AS startTimePrefixSum, " +
            "rs2.station_id AS endStationId, st2.station_name AS endStationName, " +
            "rs2.time_prefix_sum AS endTimePrefixSum " +
            "FROM sale_info s " +
            "LEFT JOIN ticket_info t ON s.ticket_id = t.ticket_id " +
            "LEFT JOIN train_info tr ON t.train_id = tr.train_id " +
            "LEFT JOIN router_station rs1 ON tr.router_id = rs1.router_id AND s.start_station_seq = rs1.station_seq " +
            "LEFT JOIN station st1 ON rs1.station_id = st1.station_id " +
            "LEFT JOIN router_station rs2 ON tr.router_id = rs2.router_id AND s.end_station_seq = rs2.station_seq " +
            "LEFT JOIN station st2 ON rs2.station_id = st2.station_id " +
            "WHERE s.user_id = #{userId} " +
            "ORDER BY s.sale_time DESC")
    Page<UserPurchaseVO> selectUserPurchasePage(Page<UserPurchaseVO> page, @Param("userId") String userId);
}