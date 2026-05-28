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
     * 根据两个站点ID查询联通信息（要求 stationAId < stationBId）
     * @param stationAId 较小的站点ID
     * @param stationBId 较大的站点ID
     * @return 联通信息
     */

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
    
    /**
     * 根据两个站点ID查询联通信息
     * @param stationAId 站点A ID（较小的ID）
     * @param stationBId 站点B ID（较大的ID）
     * @return 站点联通信息
     */
    @Select("SELECT * FROM station_connection WHERE station_a_id = #{stationAId} AND station_b_id = #{stationBId}")
    StationConnection selectByStationIds(@Param("stationAId") Integer stationAId, @Param("stationBId") Integer stationBId);
}