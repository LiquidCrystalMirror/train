package com.example.ticket.controller;

import com.example.ticket.entity.Router;
import com.example.ticket.entity.RouterStation;
import com.example.ticket.entity.Station;
import com.example.ticket.service.RouterService;
import com.example.ticket.service.RouterStationService;
import com.example.ticket.service.StationService;
import com.example.ticket.util.ApiResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 路线管理Controller
 */
@RestController
@RequestMapping("/api/v1/route")
public class RouteController {

    @Resource
    private RouterService routerService;

    @Resource
    private RouterStationService routerStationService;

    @Resource
    private StationService stationService;

    // ==================== 路线基本信息 + 站点关联 整合接口 ====================

    /**
     * 创建新路线（同时创建基本信息和站点列表）
     * @param params 包含 routerName 和 stations 数组
     *               stations 中每个元素包含 stationSeq, stationId, stayMinutes
     */
    @PostMapping("/create")
    @Transactional
    public ApiResult<Integer> createRoute(@RequestBody Map<String, Object> params) {
        String routerName = (String) params.get("routerName");
        if (routerName == null || routerName.trim().isEmpty()) {
            return ApiResult.error(400, "路线名称不能为空");
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stationsData = (List<Map<String, Object>>) params.get("stations");
        if (stationsData == null || stationsData.isEmpty()) {
            return ApiResult.error(400, "站点列表不能为空");
        }

        // 1. 创建路线基本信息
        Integer routerId = routerService.createRouter(routerName);

        // 2. 保存站点关联
        List<RouterStation> stations = stationsData.stream().map(data -> {
            RouterStation rs = new RouterStation();
            rs.setStationSeq(((Number) data.get("stationSeq")).intValue());
            rs.setStationId(((Number) data.get("stationId")).intValue());
            rs.setStayMinutes(data.getOrDefault("stayMinutes", 0) != null ?
                    ((Number) data.get("stayMinutes")).intValue() : 0);
            return rs;
        }).collect(Collectors.toList());

        boolean saved = routerStationService.saveRouterStations(routerId, stations);
        if (!saved) {
            throw new RuntimeException("保存路线站点失败");
        }

        // 新增：计算并更新总时长
        routerService.updateTotalDuration(routerId);
        return ApiResult.success("创建成功", routerId);
    }

    /**
     * 更新路线（可更新名称和站点列表）
     * @param params 包含 routerId, routerName (可选), stations (可选)
     */
    @PostMapping("/update")
    @Transactional
    public ApiResult<Void> updateRoute(@RequestBody Map<String, Object> params) {
        Integer routerId = (Integer) params.get("routerId");
        if (routerId == null || routerId == 0) {
            return ApiResult.error(400, "路线ID不能为空");
        }

        // 更新名称（如果提供）
        String routerName = (String) params.get("routerName");
        if (routerName != null && !routerName.trim().isEmpty()) {
            boolean nameUpdated = routerService.updateRouterName(routerId, routerName);
            if (!nameUpdated) {
                return ApiResult.error(400, "更新路线名称失败");
            }
        }

        // 更新站点列表（如果提供）
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stationsData = (List<Map<String, Object>>) params.get("stations");
        if (stationsData != null && !stationsData.isEmpty()) {
            List<RouterStation> stations = stationsData.stream().map(data -> {
                RouterStation rs = new RouterStation();
                rs.setStationSeq(((Number) data.get("stationSeq")).intValue());
                rs.setStationId(((Number) data.get("stationId")).intValue());
                rs.setStayMinutes(data.getOrDefault("stayMinutes", 0) != null ?
                        ((Number) data.get("stayMinutes")).intValue() : 0);
                return rs;
            }).collect(Collectors.toList());
            boolean saved = routerStationService.saveRouterStations(routerId, stations);
            if (!saved) {
                throw new RuntimeException("更新路线站点失败");
            }
        }

        // 新增：计算并更新总时长
        routerService.updateTotalDuration(routerId);
        return ApiResult.success("更新成功");
    }

    /**
     * 删除路线（同时删除基本信息和站点关联）
     */
    @PostMapping("/delete")
    @Transactional
    public ApiResult<Void> deleteRoute(@RequestBody Map<String, Integer> params) {
        Integer routerId = params.get("routerId");
        if (routerId == null) {
            return ApiResult.error(400, "路线ID不能为空");
        }

        // 先删除站点关联（如果外键未设级联，需手动删除；即使设了级联，显式删除也无害）
        routerStationService.deleteByRouterId(routerId);
        // 再删除路线基本信息
        boolean result = routerService.deleteRouter(routerId);
        return result ? ApiResult.success("删除成功") : ApiResult.error(400, "删除失败");
    }

    // ==================== 查询接口 ====================

    /**
     * 获取所有路线列表（仅基本信息）
     */
    @GetMapping("/list")
    public ApiResult<List<Router>> listRouters() {
        List<Router> routers = routerService.listAllRouters();
        return ApiResult.success("查询成功", routers);
    }

    /**
     * 获取路线详情（基本信息 + 站点列表，站点按序号排序）
     */
    @GetMapping("/detail")
    public ApiResult<Map<String, Object>> getRouteDetail(@RequestParam Integer routerId) {
        if (routerId == null) {
            return ApiResult.error(400, "路线ID不能为空");
        }

        Router router = routerService.getRouterById(routerId);
        if (router == null) {
            return ApiResult.error(404, "路线不存在");
        }

        List<RouterStation> stations = routerStationService.getStationsByRouterId(routerId);
        // 按序号排序（Service 中一般已经排序，此处可再确保）
        stations.sort((a, b) -> a.getStationSeq() - b.getStationSeq());

        Map<String, Object> detail = Map.of(
                "routerId", router.getRouterId(),
                "routerName", router.getRouterName(),
                "createTime", router.getCreateTime(),
                "totalDuration", router.getTotalDuration(),  // 新增
                "stations", stations
        );
        return ApiResult.success("查询成功", detail);
    }

    /**
     * 查询指定路线的所有站点（保留原接口，返回站点列表）
     */
    @PostMapping("/stations")
    public ApiResult<List<RouterStation>> getRouteStations(@RequestBody Map<String, Integer> params) {
        Integer routerId = params.get("routerId");
        if (routerId == null) {
            return ApiResult.error(400, "路线ID不能为空");
        }
        List<RouterStation> stations = routerStationService.getStationsByRouterId(routerId);
        return ApiResult.success("查询成功", stations);
    }

    // ==================== 站点连通性验证（保留原功能） ====================

    /**
     * 验证两个站点是否连通
     */
    @PostMapping("/validate/connection")
    public ApiResult<Boolean> validateConnection(@RequestBody Map<String, Integer> params) {
        Integer stationAId = params.get("stationAId");
        Integer stationBId = params.get("stationBId");
        if (stationAId == null || stationBId == null) {
            return ApiResult.error(400, "站点ID不能为空");
        }
        boolean connected = stationService.areStationsConnected(stationAId, stationBId);
        return ApiResult.success("验证成功", connected);
    }
}
