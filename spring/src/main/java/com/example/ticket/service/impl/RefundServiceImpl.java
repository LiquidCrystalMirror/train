package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.RefundInfo;
import com.example.ticket.mapper.RefundInfoMapper;
import com.example.ticket.service.RefundService;
import org.springframework.stereotype.Service;

@Service
public class RefundServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundService {
}