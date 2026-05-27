package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.CarriageInfo;
import com.example.ticket.mapper.CarriageInfoMapper;
import com.example.ticket.service.CarriageInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 车厢信息模板服务实现类
 */
@Service
public class CarriageInfoServiceImpl extends ServiceImpl<CarriageInfoMapper, CarriageInfo> 
        implements CarriageInfoService {

    @Override
    public List<CarriageInfo> getAllTemplates() {
        return baseMapper.selectAllTemplates();
    }

    @Override
    public boolean saveTemplates(List<CarriageInfo> templates) {
        return this.saveBatch(templates);
    }
}
