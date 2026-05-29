package com.example.ticket.mapper;

import com.example.ticket.entity.TicketInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.time.LocalDateTime;

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
}