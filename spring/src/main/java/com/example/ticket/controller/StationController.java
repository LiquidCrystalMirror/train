package com.example.ticket.controller;

import com.example.ticket.entity.Station;
import com.example.ticket.service.StationService;
import com.example.ticket.util.RespEntity;
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
    public RespEntity addStation(@RequestBody Station station) {
        boolean result = stationService.addStation(station);
        return new RespEntity(2000, result ? "添加成功" : "添加失败", null);
    }

    @DeleteMapping("/{id}")
    public RespEntity deleteStation(@PathVariable Integer id) {
        boolean result = stationService.removeById(id);
        return new RespEntity(2000, result ? "删除成功" : "删除失败", null);
    }

    @PutMapping("/update")
    public RespEntity updateStation(@RequestBody Station station) {
        boolean result = stationService.updateById(station);
        return new RespEntity(2000, result ? "更新成功" : "更新失败", null);
    }

    @GetMapping("/list")
    public RespEntity listStations() {
        List<Station> stations = stationService.list();
        return new RespEntity(2000, "查询成功", stations);
    }

    @GetMapping("/{id}")
    public RespEntity queryById(@PathVariable Integer id) {
        Station station = stationService.getById(id);
        return new RespEntity(2000, "查询成功", station);
    }

    @GetMapping("/search")
    public RespEntity queryByName(@RequestParam String name) {
        List<Station> stations = stationService.lambdaQuery()
                .like(Station::getStationName, name)
                .list();
        return new RespEntity(2000, "查询成功", stations);
    }

    @PostMapping("/connection/add")
    public RespEntity addConnection(@RequestBody Map<String, Object> params) {
        Integer stationAId = (Integer) params.get("stationAId");
        Integer stationBId = (Integer) params.get("stationBId");
        Double travelTimeMinutes = (Double) params.get("travelTimeMinutes");
        
        stationService.addConnection(stationAId, stationBId, travelTimeMinutes);
        return new RespEntity(2000, "连通关系添加成功", null);
    }

    @DeleteMapping("/connection/{stationAId}/{stationBId}")
    public RespEntity removeConnection(@PathVariable Integer stationAId, @PathVariable Integer stationBId) {
        boolean result = stationService.removeConnection(stationAId, stationBId);
        return new RespEntity(2000, result ? "连通关系删除成功" : "连通关系不存在", null);
    }

    @GetMapping("/{id}/neighbors")
    public RespEntity getNeighbors(@PathVariable Integer id) {
        List<Map<String, Object>> neighbors = stationService.getNeighborStationsWithTime(id);
        return new RespEntity(2000, "查询成功", neighbors);
    }

    @GetMapping("/check/connection/{stationAId}/{stationBId}")
    public RespEntity checkConnection(@PathVariable Integer stationAId, @PathVariable Integer stationBId) {
        boolean connected = stationService.isConnected(stationAId, stationBId);
        return new RespEntity(2000, connected ? "已连通" : "未连通", connected);
    }
}
