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
    public String add(@RequestBody TrainInfo trainInfo) {
        // 调用MyBatis-Plus的save方法
        boolean save = trainService.save(trainInfo);
        return save ? "添加成功" : "添加失败";
    }

    @PostMapping("/update")
    public String update(@RequestBody TrainInfo trainInfo) {
        // 调用MyBatis-Plus的updateById方法（需保证trainInfo含主键ID）
        boolean update = trainService.updateById(trainInfo);
        return update ? "更新成功" : "更新失败";
    }

    @PostMapping("/delete")
    public String delete(@RequestBody Map<String, Integer> params) {
        // 调用MyBatis-Plus的removeById方法
        Integer id = params.get("id");
        boolean remove = trainService.removeById(id);
        return remove ? "删除成功" : "删除失败";
    }

    @PostMapping("/query/number")
    public List<TrainInfo> queryByNumber(@RequestBody Map<String, String> params) {
        // 按车次号查询
        String number = params.get("number");
        return trainInfoMapper.selectByTrainNumber(number);
    }

    @PostMapping("/query/time")
    public List<TrainInfo> queryByTime(@RequestBody Map<String, String> params) {
        // 查询发车时间 >= 传入时间
        LocalDateTime time = LocalDateTime.parse(params.get("time"));
        return trainInfoMapper.selectByDepartureTime(time);
    }

    @PostMapping("/list")
    public RespEntity trainPage(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        String find = params.containsKey("find") ? (String) params.get("find") : "";

        Page<TrainInfo> page = new Page<>(pageNum, 6);
        trainInfoMapper.selectTrainPage(page, find);

        return new RespEntity(2000, "查询成功", page);
    }
}
