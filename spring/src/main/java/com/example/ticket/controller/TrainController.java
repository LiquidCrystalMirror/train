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
        // 验证route_id是否存在(如果有设置)
        if (trainInfo.getRouterId() != null) {
            // TODO: 可以添加验证路线是否存在的逻辑
        }
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



    @PostMapping("/list")
    public ApiResult<Page<TrainInfo>> trainPage(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        String find = params.containsKey("find") ? (String) params.get("find") : "";

        Page<TrainInfo> page = new Page<>(pageNum, 6);
        trainInfoMapper.selectTrainPage(page, find);

        return ApiResult.success("查询成功", page);
    }
    


}
