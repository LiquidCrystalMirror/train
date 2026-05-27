package com.example.ticket.controller;

import com.example.ticket.entity.RouterStation;
import com.example.ticket.entity.Station;
import com.example.ticket.service.RouterStationService;
import com.example.ticket.service.StationService;
import com.example.ticket.util.ApiResult;
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
    private RouterStationService routerStationService;

    @Resource
    private StationService stationService;

    /**
     * 查询指定路线的所有站点
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

    /**
     * 创建或更新路线站点关联
     */
    @PostMapping("/save")
    public ApiResult<Void> saveRouteStations(@RequestBody Map<String, Object> params) {
        Integer routerId = (Integer) params.get("routerId");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> stationsData = (List<Map<String, Object>>) params.get("stations");
        
        if (routerId == null || stationsData == null) {
            return ApiResult.error(400, "参数不完整");
        }
        
        // 转换为RouterStation对象
        List<RouterStation> stations = stationsData.stream().map(data -> {
            RouterStation rs = new RouterStation();
            rs.setStationSeq((Integer) data.get("stationSeq"));
            rs.setStationId((Integer) data.get("stationId"));
            rs.setStayMinutes((Integer) data.getOrDefault("stayMinutes", 0));
            return rs;
        }).toList();
        
        boolean result = routerStationService.saveRouterStations(routerId, stations);
        return result ? ApiResult.success("保存成功") : ApiResult.error(400, "保存失败");
    }

    /**
     * 删除路线
     */
    @PostMapping("/delete")
    public ApiResult<Void> deleteRoute(@RequestBody Map<String, Integer> params) {
        Integer routerId = params.get("routerId");
        if (routerId == null) {
            return ApiResult.error(400, "路线ID不能为空");
        }
        
        boolean result = routerStationService.deleteByRouterId(routerId);
        return result ? ApiResult.success("删除成功") : ApiResult.error(400, "删除失败");
    }

    /**
     * 验证站点是否联通(用于前端检查能否添加为路线站点)
     */
    @PostMapping("/validate/connection")
    public ApiResult<Boolean> validateConnection(@RequestBody Map<String, Integer> params) {
        Integer stationAId = params.get("stationAId");
        Integer stationBId = params.get("stationBId");
        
        if (stationAId == null || stationBId == null) {
            return ApiResult.error(400, "站点ID不能为空");
        }
        
        // 检查两个站点是否在联通性表中存在
        boolean connected = stationService.areStationsConnected(stationAId, stationBId);
        return ApiResult.success("验证成功", connected);
    }
}
