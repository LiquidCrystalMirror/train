package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.TicketInfo;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.service.TicketService;
import org.springframework.stereotype.Service;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketInfoMapper, TicketInfo> implements TicketService {
}