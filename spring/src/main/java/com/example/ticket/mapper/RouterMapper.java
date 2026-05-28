package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.Router;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RouterMapper extends BaseMapper<Router> {
    
    /**
     * 根据基础ID查询往返路线
     * @param baseId 基础ID（偶数）
     * @return 往程和返程路线列表
     */
    @Select("SELECT * FROM router WHERE router_id = #{baseId} OR router_id = #{baseId} + 1")
    List<Router> selectByBaseId(@Param("baseId") Long baseId);
    
    /**
     * 判断是否为往程路线（ID为偶数）
     * @param routerId 路线ID
     * @return true=往程，false=返程
     */
    default boolean isForwardRoute(Long routerId) {
        return routerId != null && (routerId & 1L) == 0L;
    }
    
    /**
     * 获取对应的返程路线ID
     * @param routerId 当前路线ID
     * @return 返程路线ID
     */
    default Long getReturnRouteId(Long routerId) {
        if (routerId == null) {
            return null;
        }
        long baseId = routerId & ~1L;
        return isForwardRoute(routerId) ? baseId + 1 : baseId;
    }
}
