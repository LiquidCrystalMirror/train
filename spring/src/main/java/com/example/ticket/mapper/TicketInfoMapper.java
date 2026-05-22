package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.TicketInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TicketInfoMapper extends BaseMapper<TicketInfo> {
    
    /**
     * 分页查询车票，支持按座位号或车厢号模糊搜索
     */
    @Select("<script>" +
            "SELECT * FROM ticket_info " +
            "<where>" +
            "<if test='find != null and find != \"\"'>" +
            "(seat_number LIKE CONCAT('%', #{find}, '%') OR carriage_number LIKE CONCAT('%', #{find}, '%'))" +
            "</if>" +
            "</where>" +
            "</script>")
    Page<TicketInfo> selectTicketPage(Page<TicketInfo> page, @Param("find") String find);
    
    /**
     * 根据火车ID查询车票
     */
    @Select("SELECT * FROM ticket_info WHERE train_id = #{trainId}")
    List<TicketInfo> selectByTrainId(@Param("trainId") Integer trainId);
    
    /**
     * 更新车票状态
     */
    @Update("UPDATE ticket_info SET ticket_status = #{status} WHERE ticket_id = #{ticketId}")
    int updateTicketStatus(@Param("ticketId") Integer ticketId, @Param("status") String status);
}