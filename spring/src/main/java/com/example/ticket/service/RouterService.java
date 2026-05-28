package com.example.ticket.service;

import com.example.ticket.entity.Router;
import com.example.ticket.entity.RouterStation;

import java.util.List;
import java.util.Map;

/**
 * 路线管理服务接口
 */
public interface RouterService {
    
    /**
     * 创建往程路线（自动生成返程路线）
     * @param routerName 路线名称
     * @param stations 站点列表（包含stationSeq, stationId, stayMinutes）
     * @return 往程路线ID
     */
    Long createRouteWithReturn(String routerName, List<Map<String, Object>> stations);
    
    /**
     * 更新路线信息
     * @param routerId 路线ID
     * @param routerName 路线名称（可选）
     * @param stations 站点列表（可选）
     * @return 是否成功
     */
    boolean updateRoute(Long routerId, String routerName, List<Map<String, Object>> stations);
    
    /**
     * 删除路线（同时删除往返路线）
     * @param routerId 路线ID
     * @return 是否成功
     */
    boolean deleteRoute(Long routerId);
    
    /**
     * 获取所有路线列表
     * @return 路线列表
     */
    List<Router> listAllRouters();
    
    /**
     * 根据ID获取路线详情
     * @param routerId 路线ID
     * @return 路线信息
     */
    Router getRouterById(Long routerId);
    
    /**
     * 获取路线的站点列表
     * @param routerId 路线ID
     * @return 站点列表（按序号排序）
     */
    List<RouterStation> getStationsByRouterId(Long routerId);
    
    /**
     * 获取往返路线对
     * @param routerId 任一方向的路线ID
     * @return 包含forwardRoute和returnRoute的Map
     */
    Map<String, Router> getRoutePair(Long routerId);
    
    /**
     * 计算并更新路线总时长
     * @param routerId 路线ID
     * @return 是否成功
     */
    boolean updateTotalDuration(Long routerId);
}
