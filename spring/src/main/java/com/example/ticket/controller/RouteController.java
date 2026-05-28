package com.example.ticket.controller;

import com.example.ticket.entity.Router;
import com.example.ticket.entity.RouterStation;
import com.example.ticket.service.RouterService;
import com.example.ticket.util.ApiResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 路线管理Controller
 */
@RestController
@RequestMapping("/api/v1/route")
public class RouteController {

    @Resource
    private RouterService routerService;

    // ==================== 核心功能：创建路线（自动生成往返） ====================

    /**
     * 创建新路线（同时自动创建往程和返程路线）
     * @param params 包含 routerName 和 stations 数组
     *               stations 中每个元素包含 stationSeq, stationId, stayMinutes
     * @return 往程路线ID
     */
    @PostMapping("/create")
    @Transactional
    public ApiResult<Long> createRoute(@RequestBody Map<String, Object> params) {
        String routerName = (String) params.get("routerName");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stationsData = (List<Map<String, Object>>) params.get("stations");
        
        Long forwardRouteId = routerService.createRouteWithReturn(routerName, stationsData);
        
        return ApiResult.success("创建成功，已自动生成往程和返程路线", forwardRouteId);
    }

    // ==================== 更新和删除 ====================

    /**
     * 更新路线信息
     * @param params 包含 routerId, routerName (可选), stations (可选)
     */
    @PostMapping("/update")
    @Transactional
    public ApiResult<Void> updateRoute(@RequestBody Map<String, Object> params) {
        Long routerId = ((Number) params.get("routerId")).longValue();
        String routerName = (String) params.get("routerName");
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stationsData = (List<Map<String, Object>>) params.get("stations");
        
        boolean result = routerService.updateRoute(routerId, routerName, stationsData);
        return result ? ApiResult.success("更新成功") : ApiResult.error(400, "更新失败");
    }

    /**
     * 删除路线（同时删除往返路线）
     */
    @PostMapping("/delete")
    @Transactional
    public ApiResult<Void> deleteRoute(@RequestBody Map<String, Object> params) {
        Long routerId = ((Number) params.get("routerId")).longValue();
        boolean result = routerService.deleteRoute(routerId);
        return result ? ApiResult.success("删除成功") : ApiResult.error(400, "删除失败");
    }

    // ==================== 查询接口 ====================

    /**
     * 获取所有路线列表
     */
    @GetMapping("/list")
    public ApiResult<List<Router>> listRouters() {
        List<Router> routers = routerService.listAllRouters();
        return ApiResult.success("查询成功", routers);
    }

    /**
     * 获取路线详情（基本信息 + 站点列表）
     */
    @GetMapping("/detail")
    public ApiResult<Map<String, Object>> getRouteDetail(@RequestParam Long routerId) {
        Router router = routerService.getRouterById(routerId);
        if (router == null) {
            return ApiResult.error(404, "路线不存在");
        }

        List<RouterStation> stations = routerService.getStationsByRouterId(routerId);
        
        Map<String, Object> detail = Map.of(
                "routerId", router.getRouterId(),
                "routerName", router.getRouterName(),
                "totalDuration", router.getTotalDuration(),
                "createTime", router.getCreateTime(),
                "isForward", (router.getRouterId() & 1L) == 0L,
                "stations", stations
        );
        return ApiResult.success("查询成功", detail);
    }

    /**
     * 获取往返路线对
     */
    @GetMapping("/pair")
    public ApiResult<Map<String, Router>> getRoutePair(@RequestParam Long routerId) {
        Map<String, Router> routePair = routerService.getRoutePair(routerId);
        return ApiResult.success("查询成功", routePair);
    }

    /**
     * 查询指定路线的所有站点
     */
    @GetMapping("/stations")
    public ApiResult<List<RouterStation>> getRouteStations(@RequestParam Long routerId) {
        List<RouterStation> stations = routerService.getStationsByRouterId(routerId);
        return ApiResult.success("查询成功", stations);
    }
}
