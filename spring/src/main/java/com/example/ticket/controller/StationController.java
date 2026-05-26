package com.example.ticket.controller;

import com.example.ticket.entity.Station;
import com.example.ticket.service.StationService;
import com.example.ticket.util.ApiResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/station")
public class StationController {

    @Resource
    private StationService stationService;

    @PostMapping("/add")
    public ApiResult<Void> addStation(@RequestBody Station station) {
        boolean result = stationService.addStation(station);
        return result ? ApiResult.success("添加成功") : ApiResult.error(400, "添加失败");
    }

    @DeleteMapping("/{id}")
    public ApiResult<Void> deleteStation(@PathVariable Integer id) {
        boolean result = stationService.removeById(id);
        return result ? ApiResult.success("删除成功") : ApiResult.error(400, "删除失败");
    }

    @PutMapping("/update")
    public ApiResult<Void> updateStation(@RequestBody Station station) {
        boolean result = stationService.updateById(station);
        return result ? ApiResult.success("更新成功") : ApiResult.error(400, "更新失败");
    }

    @GetMapping("/list")
    public ApiResult<List<Station>> listStations() {
        List<Station> stations = stationService.list();
        return ApiResult.success("查询成功", stations);
    }

    @GetMapping("/{id}")
    public ApiResult<Station> queryById(@PathVariable Integer id) {
        Station station = stationService.getById(id);
        return ApiResult.success("查询成功", station);
    }

    @GetMapping("/search")
    public ApiResult<List<Station>> queryByName(@RequestParam String name) {
        List<Station> stations = stationService.lambdaQuery()
                .like(Station::getStationName, name)
                .list();
        return ApiResult.success("查询成功", stations);
    }

    @PostMapping("/connection/add")
    public ApiResult<Void> addConnection(@RequestBody Map<String, Object> params) {
        Integer stationAId = (Integer) params.get("stationAId");
        Integer stationBId = (Integer) params.get("stationBId");
        
        // 安全地将Number转换为Double（兼容Integer和Double）
        Object travelTimeObj = params.get("travelTimeMinutes");
        Double travelTimeMinutes;
        if (travelTimeObj instanceof Integer) {
            travelTimeMinutes = ((Integer) travelTimeObj).doubleValue();
        } else if (travelTimeObj instanceof Double) {
            travelTimeMinutes = (Double) travelTimeObj;
        } else if (travelTimeObj instanceof Number) {
            travelTimeMinutes = ((Number) travelTimeObj).doubleValue();
        } else {
            throw new com.example.ticket.exception.BusinessException("行程时间格式错误");
        }
        
        stationService.addConnection(stationAId, stationBId, travelTimeMinutes);
        return ApiResult.success("连通关系添加成功");
    }

    @DeleteMapping("/connection/{stationAId}/{stationBId}")
    public ApiResult<Void> removeConnection(@PathVariable Integer stationAId, @PathVariable Integer stationBId) {
        boolean result = stationService.removeConnection(stationAId, stationBId);
        return result ? ApiResult.success("连通关系删除成功") : ApiResult.error(400, "连通关系不存在");
    }

    @GetMapping("/{id}/neighbors")
    public ApiResult<List<Map<String, Object>>> getNeighbors(@PathVariable Integer id) {
        List<Map<String, Object>> neighbors = stationService.getNeighborStationsWithTime(id);
        return ApiResult.success("查询成功", neighbors);
    }

    @GetMapping("/check/connection/{stationAId}/{stationBId}")
    public ApiResult<Boolean> checkConnection(@PathVariable Integer stationAId, @PathVariable Integer stationBId) {
        boolean connected = stationService.isConnected(stationAId, stationBId);
        return ApiResult.success(connected ? "已连通" : "未连通", connected);
    }
}
