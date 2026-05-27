package com.example.ticket.controller;

import com.example.ticket.entity.TrainScheduleWatermark;
import com.example.ticket.service.TrainScheduleWatermarkService;
import com.example.ticket.util.ApiResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 水位表管理Controller
 * 用于管理者发行车票时检查危险时间
 */
@RestController
@RequestMapping("/api/v1/watermark")
public class WatermarkController {

    @Resource
    private TrainScheduleWatermarkService watermarkService;

    /**
     * 查询指定列车的最新水位记录
     */
    @PostMapping("/latest")
    public ApiResult<TrainScheduleWatermark> getLatest(@RequestBody Map<String, String> params) {
        String trainId = params.get("trainId");
        if (trainId == null) {
            return ApiResult.error(400, "列车ID不能为空");
        }
        
        TrainScheduleWatermark watermark = watermarkService.getLatestWatermark(trainId);
        return ApiResult.success("查询成功", watermark);
    }

    /**
     * 检查指定时间段是否在危险时间内
     */
    @PostMapping("/check/danger")
    public ApiResult<Boolean> checkDangerZone(@RequestBody Map<String, Object> params) {
        String trainId = (String) params.get("trainId");
        String startTimeStr = (String) params.get("startTime");
        String endTimeStr = (String) params.get("endTime");
        
        if (trainId == null || startTimeStr == null || endTimeStr == null) {
            return ApiResult.error(400, "参数不完整");
        }
        
        LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
        LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
        
        boolean inDanger = watermarkService.isInDangerZone(trainId, startTime, endTime);
        return ApiResult.success("检查成功", inDanger);
    }

    /**
     * 获取可以出票的最早时间
     */
    @PostMapping("/earliest/ticket/time")
    public ApiResult<String> getEarliestTicketTime(@RequestBody Map<String, String> params) {
        String trainId = params.get("trainId");
        if (trainId == null) {
            return ApiResult.error(400, "列车ID不能为空");
        }
        
        LocalDateTime earliestTime = watermarkService.getEarliestTicketTime(trainId);
        return ApiResult.success("查询成功", earliestTime.toString());
    }

    /**
     * 更新水位记录(管理员在发行新车票后调用)
     */
    @PostMapping("/update")
    public ApiResult<Void> updateWatermark(@RequestBody Map<String, Object> params) {
        String trainId = (String) params.get("trainId");
        Integer routeId = (Integer) params.get("routeId");
        String departTimeStr = (String) params.get("departTime");
        String arriveTimeStr = (String) params.get("arriveTime");
        Integer updatedBy = (Integer) params.get("updatedBy");
        
        if (trainId == null || routeId == null || departTimeStr == null || 
            arriveTimeStr == null || updatedBy == null) {
            return ApiResult.error(400, "参数不完整");
        }
        
        LocalDateTime departTime = LocalDateTime.parse(departTimeStr);
        LocalDateTime arriveTime = LocalDateTime.parse(arriveTimeStr);
        
        boolean result = watermarkService.updateWatermark(trainId, routeId, departTime, arriveTime, updatedBy);
        return result ? ApiResult.success("更新成功") : ApiResult.error(400, "更新失败");
    }
}
