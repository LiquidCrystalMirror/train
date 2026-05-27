package com.example.ticket.controller;

import com.example.ticket.entity.DepartureSchedule;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.service.DepartureScheduleService;
import com.example.ticket.service.TrainService;
import com.example.ticket.util.ApiResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 车次发车时间管理Controller
 */
@RestController
@RequestMapping("/api/v1/departure")
public class DepartureController {

    @Resource
    private DepartureScheduleService departureScheduleService;

    @Resource
    private TrainService trainService;

    /**
     * 查询指定列车的所有发车时间
     */
    @PostMapping("/list")
    public ApiResult<List<DepartureSchedule>> getSchedules(@RequestBody Map<String, Integer> params) {
        Integer trainId = params.get("trainId");
        if (trainId == null) {
            return ApiResult.error(400, "列车ID不能为空");
        }
        
        List<DepartureSchedule> schedules = departureScheduleService.getSchedulesByTrainId(trainId);
        return ApiResult.success("查询成功", schedules);
    }

    /**
     * 根据时间范围查询车次
     */
    @PostMapping("/query/timeRange")
    public ApiResult<List<DepartureSchedule>> queryByTimeRange(@RequestBody Map<String, String> params) {
        String startTimeStr = params.get("startTime");
        String endTimeStr = params.get("endTime");
        
        if (startTimeStr == null || endTimeStr == null) {
            return ApiResult.error(400, "时间参数不能为空");
        }
        
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
        
        List<DepartureSchedule> schedules = departureScheduleService.getSchedulesByTimeRange(startTime, endTime);
        return ApiResult.success("查询成功", schedules);
    }

    /**
     * 创建发车时间表
     */
    @PostMapping("/create")
    public ApiResult<Void> createSchedule(@RequestBody Map<String, Object> params) {
        Integer trainId = (Integer) params.get("trainId");
        String departureTimeStr = (String) params.get("departureTime");
        Integer direction = (Integer) params.get("direction");
        
        if (trainId == null || departureTimeStr == null || direction == null) {
            return ApiResult.error(400, "参数不完整");
        }
        
        // 检查列车是否存在
        TrainInfo train = trainService.getById(trainId);
        if (train == null) {
            return ApiResult.error(400, "列车不存在");
        }
        
        LocalDateTime departureTime = LocalDateTime.parse(departureTimeStr);
        
        // 检查是否有时间冲突
        if (departureScheduleService.hasConflict(trainId, departureTime, departureTime.plusHours(1))) {
            return ApiResult.error(400, "该时间段已有车次安排");
        }
        
        DepartureSchedule schedule = new DepartureSchedule();
        schedule.setTrainId(trainId);
        schedule.setTrainName(train.getTrainNumber());
        schedule.setDepartureTime(departureTime);
        schedule.setDirection(direction);
        
        boolean result = departureScheduleService.createSchedule(schedule);
        return result ? ApiResult.success("创建成功") : ApiResult.error(400, "创建失败");
    }

    /**
     * 删除发车时间表
     */
    @PostMapping("/delete")
    public ApiResult<Void> deleteSchedule(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");
        if (id == null) {
            return ApiResult.error(400, "ID不能为空");
        }
        
        boolean result = departureScheduleService.removeById(id);
        return result ? ApiResult.success("删除成功") : ApiResult.error(400, "删除失败");
    }
}
