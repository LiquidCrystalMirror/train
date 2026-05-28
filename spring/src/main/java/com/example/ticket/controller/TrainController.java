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
    public ApiResult<Integer> add(@RequestBody Map<String, Object> params) {
        String trainNumber = (String) params.get("trainNumber");
        Long routerId = params.get("routerId") != null ? 
                ((Number) params.get("routerId")).longValue() : null;
        
        Integer forwardTrainId = trainService.createTrainWithReturn(trainNumber, routerId);
        return ApiResult.success("添加成功，往返列车已创建", forwardTrainId);
    }

    @PostMapping("/update")
    public ApiResult<Void> update(@RequestBody Map<String, Object> params) {
        Integer trainId = params.get("trainId") != null ? 
                ((Number) params.get("trainId")).intValue() : null;
        String trainNumber = (String) params.get("trainNumber");
        Long routerId = params.get("routerId") != null ? 
                ((Number) params.get("routerId")).longValue() : null;
        
        if (trainId == null) {
            return ApiResult.error(400, "列车ID不能为空");
        }
        
        boolean success = trainService.updateTrainWithReturn(trainId, trainNumber, routerId);
        return success ? ApiResult.success("更新成功，往返列车已同步更新") : ApiResult.error(400, "更新失败");
    }

    @PostMapping("/delete")
    public ApiResult<Void> delete(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");
        if (id == null) {
            return ApiResult.error(400, "列车ID不能为空");
        }
        boolean success = trainService.deleteTrainWithReturn(id);
        return success ? ApiResult.success("删除成功，往返列车已同步删除") : ApiResult.error(400, "删除失败");
    }

    @PostMapping("/query/number")
    public ApiResult<List<TrainInfo>> queryByNumber(@RequestBody Map<String, String> params) {
        String number = params.get("number");
        List<TrainInfo> trains = trainInfoMapper.selectByTrainNumber(number);
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
    


}
