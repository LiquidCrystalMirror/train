package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.mapper.TrainInfoMapper;
import com.example.ticket.service.TrainService;
import org.springframework.stereotype.Service;

@Service
public class TrainServiceImpl extends ServiceImpl<TrainInfoMapper, TrainInfo> implements TrainService {
}