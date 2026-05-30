package com.example.ticket.mapper;

import com.example.ticket.entity.TicketInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TicketInventoryMapper extends BaseMapper<TicketInventory> {

    @Update("UPDATE ticket_inventory " +
            "SET remaining_count = remaining_count - 1, sold_count = sold_count + 1, update_time = NOW() " +
            "WHERE train_id = #{trainId} AND departure_time = #{departureTime} " +
            "AND seat_type = #{seatType} AND remaining_count > 0 AND remaining_count = #{oldRemaining}")
    int deductStock(@Param("trainId") Integer trainId,
                    @Param("departureTime") LocalDateTime departureTime,
                    @Param("seatType") Long seatType,
                    @Param("oldRemaining") Integer oldRemaining);

    @Update("UPDATE ticket_inventory " +
            "SET remaining_count = remaining_count + 1, sold_count = sold_count - 1, update_time = NOW() " +
            "WHERE train_id = #{trainId} AND departure_time = #{departureTime} AND seat_type = #{seatType}")
    int restoreStock(@Param("trainId") Integer trainId,
                     @Param("departureTime") LocalDateTime departureTime,
                     @Param("seatType") Long seatType);

    /**
     * 按车次+发车时间查询库存（按座位类型分组）
     */
    @Select("SELECT * FROM ticket_inventory " +
            "WHERE train_id = #{trainId} AND departure_time = #{departureTime} " +
            "ORDER BY seat_type")
    List<TicketInventory> selectByTrainAndDeparture(@Param("trainId") Integer trainId,
                                                     @Param("departureTime") LocalDateTime departureTime);
}