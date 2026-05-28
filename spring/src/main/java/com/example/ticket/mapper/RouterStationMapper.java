package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.RouterStation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RouterStationMapper extends BaseMapper<RouterStation> {
    
    /**
     * 根据路线ID查询站点列表（按序号排序）
     * @param routerId 路线ID
     * @return 站点列表
     */
    @Select("SELECT * FROM router_station WHERE router_id = #{routerId} ORDER BY station_seq ASC")
    List<RouterStation> selectByRouterId(@Param("routerId") Long routerId);
    
    /**
     * 删除指定路线的所有站点
     * @param routerId 路线ID
     * @return 删除的记录数
     */
    int deleteByRouterId(@Param("routerId") Long routerId);
}
