package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.SaleInfo;
import com.example.ticket.mapper.SaleInfoMapper;
import com.example.ticket.service.SaleService;
import org.springframework.stereotype.Service;

@Service
public class SaleServiceImpl extends ServiceImpl<SaleInfoMapper, SaleInfo> implements SaleService {
}