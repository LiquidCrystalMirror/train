package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.RouterStation;

import java.util.List;

/**
 * 路线站点服务接口
 */
public interface RouterStationService extends IService<RouterStation> {
    
    /**
     * 根据路线ID查询该路线的所有站点(按序号排序)
     */
    List<RouterStation> getStationsByRouterId(Integer routerId);
    
    /**
     * 创建或更新路线站点关联
     */
    boolean saveRouterStations(Integer routerId, List<RouterStation> stations);
    
    /**
     * 删除指定路线的所有站点关联
     */
    boolean deleteByRouterId(Integer routerId);

    // RouterStationService 接口增加
    Double calculateTotalDuration(Integer routerId);
}
