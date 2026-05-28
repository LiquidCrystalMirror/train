package com.example.ticket.service;

import com.example.ticket.entity.RouterStation;

import java.util.List;
import java.util.Map;

/**
 * 路线站点管理服务接口
 */
public interface RouterStationService {
    
    /**
     * 保存路线站点列表
     * @param routerId 路线ID
     * @param stations 站点列表
     * @return 是否成功
     */
    boolean saveRouterStations(Long routerId, List<RouterStation> stations);
    
    /**
     * 根据路线ID查询站点列表
     * @param routerId 路线ID
     * @return 站点列表（按序号排序）
     */
    List<RouterStation> getStationsByRouterId(Long routerId);
    
    /**
     * 删除指定路线的所有站点
     * @param routerId 路线ID
     * @return 是否成功
     */
    boolean deleteByRouterId(Long routerId);
    
    /**
     * 计算路线总时长（基于站点联通表的时间）
     * @param routerId 路线ID
     * @return 总时长（分钟）
     */
    Double calculateTotalDuration(Long routerId);
    
    /**
     * 反转站点列表（用于生成返程路线）
     * @param stations 原站点列表
     * @return 反转后的站点列表
     */
    List<RouterStation> reverseStations(List<RouterStation> stations);
}
