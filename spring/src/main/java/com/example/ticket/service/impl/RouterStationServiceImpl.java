package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.RouterStation;
import com.example.ticket.entity.StationConnection;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.RouterStationMapper;
import com.example.ticket.mapper.StationConnectionMapper;
import com.example.ticket.service.RouterStationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 路线站点服务实现类
 */
@Service
public class RouterStationServiceImpl extends ServiceImpl<RouterStationMapper, RouterStation> 
        implements RouterStationService {

    @Resource
    private StationConnectionMapper stationConnectionMapper;

    @Override
    public List<RouterStation> getStationsByRouterId(Integer routerId) {
        return baseMapper.selectByRouterId(routerId);
    }

    @Override
    @Transactional
    public boolean saveRouterStations(Integer routerId, List<RouterStation> stations) {
        if (routerId == null || routerId == 0) {
            throw new BusinessException("路线ID不能为空，请先创建路线");
        }
        
        // 验证站点连通性
        if (stations != null && stations.size() >= 2) {
            for (int i = 0; i < stations.size() - 1; i++) {
                Integer fromStationId = stations.get(i).getStationId();
                Integer toStationId = stations.get(i + 1).getStationId();
                
                Double travelTime = getTravelTimeBetweenStations(fromStationId, toStationId);
                if (travelTime == null) {
                    throw new BusinessException(
                        String.format("站点 %d 与站点 %d 之间未连通，无法保存路线", fromStationId, toStationId)
                    );
                }
            }
        }
        
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

    @Override
    public Double calculateTotalDuration(Integer routerId) {
        // 1. 获取该路线的所有站点（按序号排序）
        List<RouterStation> stations = getStationsByRouterId(routerId);
        if (stations == null || stations.size() < 2) {
            return 0.0;
        }

        double totalMinutes = 0.0;

        // 2. 累加相邻站点间的通行时间 + 中间站点的停留时间
        for (int i = 0; i < stations.size() - 1; i++) {
            int fromId = stations.get(i).getStationId();
            int toId = stations.get(i + 1).getStationId();
            
            // 获取两站间的通行时间
            Double travelTime = getTravelTimeBetweenStations(fromId, toId);
            if (travelTime == null) {
                throw new BusinessException(
                    String.format("站点 %d 与站点 %d 之间未连通，无法计算总时长", fromId, toId)
                );
            }
            totalMinutes += travelTime;
            
            // 累加当前站点的停留时间（最后一个站点除外）
            totalMinutes += stations.get(i).getStayMinutes();
        }

        return totalMinutes;
    }

    /**
     * 获取两站点间的通行时间（无向图）
     * 注意：station_connection表要求 station_a_id < station_b_id
     */
    private Double getTravelTimeBetweenStations(Integer stationAId, Integer stationBId) {
        int a = stationAId;
        int b = stationBId;
        
        // 确保 a < b（符合数据库约束）
        if (a > b) {
            int tmp = a;
            a = b;
            b = tmp;
        }
        
        LambdaQueryWrapper<StationConnection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StationConnection::getStationAId, a)
                .eq(StationConnection::getStationBId, b);
        
        StationConnection conn = stationConnectionMapper.selectOne(wrapper);
        return conn != null ? conn.getTravelTimeMinutes() : null;
    }
}
