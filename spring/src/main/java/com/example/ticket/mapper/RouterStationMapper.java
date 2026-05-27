package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.RouterStation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 路线站点关联Mapper
 */
@Mapper
public interface RouterStationMapper extends BaseMapper<RouterStation> {
    
    /**
     * 根据路线ID查询该路线的所有站点(按序号排序)
     */
    List<RouterStation> selectByRouterId(@Param("routerId") Integer routerId);
    
    /**
     * 删除指定路线的所有站点关联
     */
    int deleteByRouterId(@Param("routerId") Integer routerId);
}
