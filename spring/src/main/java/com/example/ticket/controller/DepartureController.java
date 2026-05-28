package com.example.ticket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.DepartureSchedule;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.service.DepartureScheduleService;
import com.example.ticket.service.TrainService;
import com.example.ticket.util.ApiResult;
import com.example.ticket.vo.TrainScheduleQueryVO;
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
     * 创建发车时间表（带完整验证）
     */
    @PostMapping("/create")
    public ApiResult<Map<String, Object>> createSchedule(@RequestBody Map<String, Object> params) {
        Integer trainId = (Integer) params.get("trainId");
        String departureTimeStr = (String) params.get("departureTime");
        Long routerId = params.get("routerId") != null ? ((Number) params.get("routerId")).longValue() : null;
        
        if (trainId == null || departureTimeStr == null) {
            return ApiResult.error(400, "参数不完整");
        }
        
        LocalDateTime departureTime = LocalDateTime.parse(departureTimeStr);
        
        // 使用新的验证方法
        Map<String, Object> result = departureScheduleService.createScheduleWithValidation(
                trainId, departureTime, routerId);
        
        Boolean success = (Boolean) result.get("success");
        String message = (String) result.get("message");
        
        if (success) {
            return ApiResult.success(message, result);
        } else {
            return ApiResult.error(400, message);
        }
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


    /**
     * 查询直达车次
     * 请求体示例：
     * {
     *   "startStationId": 1,
     *   "endStationId": 5,
     *   "startTime": "2025-05-28T10:00:00",
     *   "pageNum": 1,
     *   "pageSize": 10
     * }
     */
    @PostMapping("/queryByStations")
    public ApiResult<Page<TrainScheduleQueryVO>> queryByStations(@RequestBody Map<String, Object> params) {
        Integer startStationId = params.get("startStationId") != null ?
                ((Number) params.get("startStationId")).intValue() : null;
        Integer endStationId = params.get("endStationId") != null ?
                ((Number) params.get("endStationId")).intValue() : null;
        LocalDateTime startTime = params.get("startTime") != null ?
                LocalDateTime.parse((String) params.get("startTime")) : null;
        Integer pageNum = params.get("pageNum") != null ?
                ((Number) params.get("pageNum")).intValue() : null;
        Integer pageSize = params.get("pageSize") != null ?
                ((Number) params.get("pageSize")).intValue() : null;

        Page<TrainScheduleQueryVO> page = departureScheduleService.querySchedulesByStations(
                startStationId, endStationId, startTime, pageNum, pageSize);
        return ApiResult.success("查询成功", page);
    }
}
