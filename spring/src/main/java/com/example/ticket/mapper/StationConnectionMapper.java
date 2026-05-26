package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.StationConnection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StationConnectionMapper extends BaseMapper<StationConnection> {

    /**
     * 查询某个站点的所有邻站ID及通行时间（无向图）
     * @param stationId 站点ID
     * @return List<Map> 包含 neighborStationId, travelTimeMinutes
     */
    @Select("SELECT " +
            "  CASE WHEN station_a_id = #{stationId} THEN station_b_id ELSE station_a_id END AS neighbor_station_id, " +
            "  travel_time_minutes " +
            "FROM station_connection " +
            "WHERE station_a_id = #{stationId} OR station_b_id = #{stationId}")
    List<Map<String, Object>> findNeighborsByStationId(@Param("stationId") Integer stationId);
}