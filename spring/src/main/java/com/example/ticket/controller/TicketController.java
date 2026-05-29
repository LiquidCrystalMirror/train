package com.example.ticket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.TicketInfo;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.service.TicketService;
import com.example.ticket.util.ApiResult;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ticket")
public class TicketController {

    @Resource
    private TicketService ticketService;
    
    @Resource
    private TicketInfoMapper ticketInfoMapper;

    // ===================== 【新加】车票列表 =====================
    @PostMapping("/list")
    public ApiResult<Page<TicketInfo>> ticketPage(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        String find = params.containsKey("find") ? (String) params.get("find") : "";

        Page<TicketInfo> page = new Page<>(pageNum, 6);
        ticketInfoMapper.selectTicketPage(page, find);

        return ApiResult.success("查询成功", page);
    }



    // 根据火车ID查车票
    @PostMapping("/train")
    public ApiResult<List<TicketInfo>> getByTrain(@RequestBody Map<String, Integer> params) {
        Integer trainId = params.get("trainId");
        List<TicketInfo> tickets = ticketInfoMapper.selectByTrainId(trainId);
        return ApiResult.success("查询成功", tickets);
    }

//    // 修改车票
//    @PostMapping("/update")
//    public ApiResult<Void> update(@RequestBody TicketInfo ticketInfo) {
//        boolean update = ticketService.updateById(ticketInfo);
//        return update ? ApiResult.success("修改车票成功") : ApiResult.error(400, "修改失败");
//    }
//
//    // 删除车票
//    @PostMapping("/delete")
//    public ApiResult<Void> delete(@RequestBody Map<String, Integer> params) {
//        Integer id = params.get("id");
//        boolean remove = ticketService.removeById(id);
//        return remove ? ApiResult.success("删除车票成功") : ApiResult.error(400, "删除失败");
//    }
//
//    // 添加车票
//    @PostMapping("/add")
//    public ApiResult<Void> add(@RequestBody TicketInfo ticketInfo) {
//        boolean save = ticketService.save(ticketInfo);
//        return save ? ApiResult.success("添加车票成功") : ApiResult.error(400, "添加失败");
//    }
}