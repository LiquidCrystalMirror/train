package com.example.ticket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.mapper.TrainInfoMapper;
import com.example.ticket.service.TrainService;
import com.example.ticket.util.ApiResult;
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
    public ApiResult<Void> add(@RequestBody TrainInfo trainInfo) {
        boolean save = trainService.save(trainInfo);
        return save ? ApiResult.success("添加成功") : ApiResult.error(400, "添加失败");
    }

    @PostMapping("/update")
    public ApiResult<Void> update(@RequestBody TrainInfo trainInfo) {
        boolean update = trainService.updateById(trainInfo);
        return update ? ApiResult.success("更新成功") : ApiResult.error(400, "更新失败");
    }

    @PostMapping("/delete")
    public ApiResult<Void> delete(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");
        boolean remove = trainService.removeById(id);
        return remove ? ApiResult.success("删除成功") : ApiResult.error(400, "删除失败");
    }

    @PostMapping("/query/number")
    public ApiResult<List<TrainInfo>> queryByNumber(@RequestBody Map<String, String> params) {
        String number = params.get("number");
        List<TrainInfo> trains = trainInfoMapper.selectByTrainNumber(number);
        return ApiResult.success("查询成功", trains);
    }

    @PostMapping("/query/time")
    public ApiResult<List<TrainInfo>> queryByTime(@RequestBody Map<String, String> params) {
        LocalDateTime time = LocalDateTime.parse(params.get("time"));
        List<TrainInfo> trains = trainInfoMapper.selectByDepartureTime(time);
        return ApiResult.success("查询成功", trains);
    }

    @PostMapping("/list")
    public ApiResult<Page<TrainInfo>> trainPage(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        String find = params.containsKey("find") ? (String) params.get("find") : "";

        Page<TrainInfo> page = new Page<>(pageNum, 6);
        trainInfoMapper.selectTrainPage(page, find);

        return ApiResult.success("查询成功", page);
    }
    
    /**
     * 根据起止站点查询车次
     */
    @PostMapping("/query/stations")
    public ApiResult<List<TrainInfo>> queryByStations(@RequestBody Map<String, Integer> params) {
        Integer startStationId = params.get("startStationId");
        Integer endStationId = params.get("endStationId");
        
        if (startStationId == null || endStationId == null) {
            throw new com.example.ticket.exception.BusinessException("起点站和终点站ID不能为空");
        }
        
        List<TrainInfo> trains = trainInfoMapper.selectByStations(startStationId, endStationId);
        return ApiResult.success("查询成功", trains);
    }
    
    /**
     * 根据发车时间范围查询车次
     */
    @PostMapping("/query/timeRange")
    public ApiResult<List<TrainInfo>> queryByTimeRange(@RequestBody Map<String, String> params) {
        String startTimeStr = params.get("startTime");
        String endTimeStr = params.get("endTime");
        
        if (startTimeStr == null || endTimeStr == null) {
            throw new com.example.ticket.exception.BusinessException("开始时间和结束时间不能为空");
        }
        
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
        
        List<TrainInfo> trains = trainInfoMapper.selectByDepartureTimeRange(startTime, endTime);
        return ApiResult.success("查询成功", trains);
    }
}
