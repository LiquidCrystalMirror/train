package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.DepartureSchedule;
import com.example.ticket.vo.TrainScheduleQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 车次发车时间Mapper
 */
@Mapper
public interface DepartureScheduleMapper extends BaseMapper<DepartureSchedule> {
    
    /**
     * 根据列车ID查询所有发车时间
     */
    List<DepartureSchedule> selectByTrainId(@Param("trainId") Integer trainId);
    
    /**
     * 根据发车时间范围查询车次
     */
    List<DepartureSchedule> selectByDepartureTimeRange(@Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);
    
    /**
     * 检查指定时间段是否有冲突的车次
     */
    List<DepartureSchedule> selectConflictSchedules(@Param("trainId") Integer trainId,
                                                     @Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询按顺序经过起点站和终点站，且到达起点站时间晚于指定时间的车次班次
     * @param page 分页对象
     * @param startStationId 起点站ID
     * @param endStationId 终点站ID
     * @param startTime 起始时间（到达起点站的时间需 > startTime）
     * @return 分页结果
     */
    @Select("SELECT ds.train_id, ds.train_number, ds.departure_time, ds.router_id, r.router_name, " +
            "DATE_ADD(ds.departure_time, INTERVAL CAST(rs1.time_prefix_sum AS SIGNED) MINUTE) AS start_arrival_time, " +
            "DATE_ADD(ds.departure_time, INTERVAL CAST(rs2.time_prefix_sum AS SIGNED) MINUTE) AS end_arrival_time " +
            "FROM departure_schedule ds " +
            "INNER JOIN router_station rs1 ON ds.router_id = rs1.router_id AND rs1.station_id = #{startStationId} " +
            "INNER JOIN router_station rs2 ON ds.router_id = rs2.router_id AND rs2.station_id = #{endStationId} " +
            "INNER JOIN router r ON ds.router_id = r.router_id " +
            "WHERE rs1.station_seq < rs2.station_seq " +
            "AND DATE_ADD(ds.departure_time, INTERVAL CAST(rs1.time_prefix_sum AS SIGNED) MINUTE) > #{startTime} " +
            "ORDER BY start_arrival_time ASC")
    Page<TrainScheduleQueryVO> selectSchedulesByStartEndStationAndTime(Page<?> page,
                                                                       @Param("startStationId") Integer startStationId,
                                                                       @Param("endStationId") Integer endStationId,
                                                                       @Param("startTime") LocalDateTime startTime);

}
