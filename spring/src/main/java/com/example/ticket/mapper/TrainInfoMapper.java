package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.TrainInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TrainInfoMapper extends BaseMapper<TrainInfo> {
    
    /**
     * 按车次号查询
     */
    @Select("SELECT * FROM train_info WHERE train_number = #{trainNumber}")
    List<TrainInfo> selectByTrainNumber(@Param("trainNumber") String trainNumber);
    
    /**
     * 分页查询车次，支持按车次号模糊搜索
     * 注意：新结构中不再有startStation和endStation字段，如需按站点查询需关联train_station表
     */
    @Select("<script>" +
            "SELECT * FROM train_info " +
            "<where>" +
            "<if test='find != null and find != \"\"'>" +
            "train_number LIKE CONCAT('%', #{find}, '%')" +
            "</if>" +
            "</where>" +
            "</script>")
    Page<TrainInfo> selectTrainPage(Page<TrainInfo> page, @Param("find") String find);
    
    /**
     * 查询发车时间大于等于指定时间的车次
     */
    @Select("SELECT * FROM train_info WHERE departure_time >= #{departureTime}")
    List<TrainInfo> selectByDepartureTime(@Param("departureTime") LocalDateTime departureTime);
    
    /**
     * 根据起止站点查询车次（需要关联train_station表）
     * @param startStationId 起点站ID
     * @param endStationId 终点站ID
     */
    @Select("SELECT DISTINCT t.* FROM train_info t " +
            "INNER JOIN train_station ts1 ON t.train_id = ts1.train_id " +
            "INNER JOIN train_station ts2 ON t.train_id = ts2.train_id " +
            "WHERE ts1.station_id = #{startStationId} " +
            "AND ts2.station_id = #{endStationId} " +
            "AND ts1.station_seq < ts2.station_seq")
    List<TrainInfo> selectByStations(@Param("startStationId") Integer startStationId, 
                                     @Param("endStationId") Integer endStationId);
    
    /**
     * 根据发车时间范围查询车次
     */
    @Select("SELECT * FROM train_info WHERE departure_time BETWEEN #{startTime} AND #{endTime}")
    List<TrainInfo> selectByDepartureTimeRange(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据路线ID查询列车
     * @param routerId 路线ID
     * @return 列车信息
     */
    @Select("SELECT * FROM train_info WHERE router_id = #{routerId}")
    TrainInfo selectByRouterId(@Param("routerId") Long routerId);
}