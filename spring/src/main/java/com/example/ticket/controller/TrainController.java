package com.example.ticket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.mapper.TrainInfoMapper;
import com.example.ticket.service.TrainService;
import com.example.ticket.util.RespEntity;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/train")
public class TrainController {

    @Resource
    private TrainService trainService;
    
    @Resource
    private TrainInfoMapper trainInfoMapper;

    @PostMapping("/add")
    public RespEntity add(@RequestBody TrainInfo trainInfo) {
        boolean save = trainService.save(trainInfo);
        return new RespEntity(2000, save ? "添加成功" : "添加失败", null);
    }

    @PostMapping("/update")
    public RespEntity update(@RequestBody TrainInfo trainInfo) {
        boolean update = trainService.updateById(trainInfo);
        return new RespEntity(2000, update ? "更新成功" : "更新失败", null);
    }

    @PostMapping("/delete")
    public RespEntity delete(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");
        boolean remove = trainService.removeById(id);
        return new RespEntity(2000, remove ? "删除成功" : "删除失败", null);
    }

    @PostMapping("/query/number")
    public RespEntity queryByNumber(@RequestBody Map<String, String> params) {
        String number = params.get("number");
        List<TrainInfo> trains = trainInfoMapper.selectByTrainNumber(number);
        return new RespEntity(2000, "查询成功", trains);
    }

    @PostMapping("/query/time")
    public RespEntity queryByTime(@RequestBody Map<String, String> params) {
        LocalDateTime time = LocalDateTime.parse(params.get("time"));
        List<TrainInfo> trains = trainInfoMapper.selectByDepartureTime(time);
        return new RespEntity(2000, "查询成功", trains);
    }

    @PostMapping("/list")
    public RespEntity trainPage(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        String find = params.containsKey("find") ? (String) params.get("find") : "";

        Page<TrainInfo> page = new Page<>(pageNum, 6);
        trainInfoMapper.selectTrainPage(page, find);

        return new RespEntity(2000, "查询成功", page);
    }
    
    /**
     * 根据起止站点查询车次
     */
    @PostMapping("/query/stations")
    public RespEntity queryByStations(@RequestBody Map<String, Integer> params) {
        Integer startStationId = params.get("startStationId");
        Integer endStationId = params.get("endStationId");
        
        if (startStationId == null || endStationId == null) {
            throw new com.example.ticket.exception.BusinessException("起点站和终点站ID不能为空");
        }
        
        List<TrainInfo> trains = trainInfoMapper.selectByStations(startStationId, endStationId);
        return new RespEntity(2000, "查询成功", trains);
    }
    
    /**
     * 根据发车时间范围查询车次
     */
    @PostMapping("/query/timeRange")
    public RespEntity queryByTimeRange(@RequestBody Map<String, String> params) {
        String startTimeStr = params.get("startTime");
        String endTimeStr = params.get("endTime");
        
        if (startTimeStr == null || endTimeStr == null) {
            throw new com.example.ticket.exception.BusinessException("开始时间和结束时间不能为空");
        }
        
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
        
        List<TrainInfo> trains = trainInfoMapper.selectByDepartureTimeRange(startTime, endTime);
        return new RespEntity(2000, "查询成功", trains);
    }
}
