package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.RouterStation;
import com.example.ticket.mapper.RouterStationMapper;
import com.example.ticket.service.RouterStationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 路线站点服务实现类
 */
@Service
public class RouterStationServiceImpl extends ServiceImpl<RouterStationMapper, RouterStation> 
        implements RouterStationService {

    @Override
    public List<RouterStation> getStationsByRouterId(Integer routerId) {
        return baseMapper.selectByRouterId(routerId);
    }

    @Override
    @Transactional
    public boolean saveRouterStations(Integer routerId, List<RouterStation> stations) {
        // 先删除旧的关联
        baseMapper.deleteByRouterId(routerId);
        
        // 设置routerId并批量插入
        for (RouterStation station : stations) {
            station.setRouterId(routerId);
        }
        
        return this.saveBatch(stations);
    }

    @Override
    @Transactional
    public boolean deleteByRouterId(Integer routerId) {
        return baseMapper.deleteByRouterId(routerId) > 0;
    }
}
