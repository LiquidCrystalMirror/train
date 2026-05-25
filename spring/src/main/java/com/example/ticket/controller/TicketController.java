package com.example.ticket.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.TicketInfo;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.service.TicketService;
import com.example.ticket.util.RespEntity;
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
    public RespEntity ticketPage(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        String find = params.containsKey("find") ? (String) params.get("find") : "";

        Page<TicketInfo> page = new Page<>(pageNum, 6);
        ticketInfoMapper.selectTicketPage(page, find);

        return new RespEntity(2000, "查询成功", page);
    }

    // 添加车票
    @PostMapping("/add")
    public RespEntity add(@RequestBody TicketInfo ticketInfo) {
        boolean save = ticketService.save(ticketInfo);
        return new RespEntity(2000, save ? "添加车票成功" : "添加失败", null);
    }

    // 根据火车ID查车票
    @PostMapping("/train")
    public RespEntity getByTrain(@RequestBody Map<String, Integer> params) {
        Integer trainId = params.get("trainId");
        List<TicketInfo> tickets = ticketInfoMapper.selectByTrainId(trainId);
        return new RespEntity(2000, "查询成功", tickets);
    }

    // 修改车票
    @PostMapping("/update")
    public RespEntity update(@RequestBody TicketInfo ticketInfo) {
        boolean update = ticketService.updateById(ticketInfo);
        return new RespEntity(2000, update ? "修改车票成功" : "修改失败", null);
    }

    // 删除车票
    @PostMapping("/delete")
    public RespEntity delete(@RequestBody Map<String, Integer> params) {
        Integer id = params.get("id");
        boolean remove = ticketService.removeById(id);
        return new RespEntity(2000, remove ? "删除车票成功" : "删除失败", null);
    }
}