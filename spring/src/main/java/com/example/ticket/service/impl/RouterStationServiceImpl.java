package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class RouterStationServiceImpl extends ServiceImpl<RouterStationMapper, RouterStation> implements RouterStationService {

    @Resource
    private StationConnectionMapper stationConnectionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveRouterStations(Long routerId, List<RouterStation> stations) {
        if (stations == null || stations.isEmpty()) {
            return true;
        }
        
        // 先删除原有站点
        this.getBaseMapper().deleteByRouterId(routerId);
        
        // 设置routerId并按序号排序
        stations.sort(Comparator.comparingInt(RouterStation::getStationSeq));
        
        // 计算timePrefixSum（时间前缀和）
        calculateTimePrefixSum(stations);
        
        // 批量插入
        for (RouterStation station : stations) {
            station.setRouterId(routerId);
            this.save(station);
        }
        
        return true;
    }

    @Override
    public List<RouterStation> getStationsByRouterId(Long routerId) {
        return this.getBaseMapper().selectByRouterId(routerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByRouterId(Long routerId) {
        int deleted = this.getBaseMapper().deleteByRouterId(routerId);
        return deleted >= 0;
    }

    @Override
    public Double calculateTotalDuration(Long routerId) {
        List<RouterStation> stations = getStationsByRouterId(routerId);
        if (stations == null || stations.size() < 2) {
            return 0.0;
        }
        
        double totalMinutes = 0.0;
        
        // 遍历相邻站点，累加通行时间
        for (int i = 0; i < stations.size() - 1; i++) {
            Integer stationAId = stations.get(i).getStationId();
            Integer stationBId = stations.get(i + 1).getStationId();
            
            // 查询站点间的通行时间
            Double travelTime = getTravelTimeBetweenStations(stationAId, stationBId);
            if (travelTime == null) {
                throw new BusinessException(String.format(
                    "站点 %d 和站点 %d 之间没有联通关系", stationAId, stationBId
                ));
            }
            
            totalMinutes += travelTime;
            
            // 加上在站点的停留时间（除了最后一个站点）
            if (i < stations.size() - 1) {
                Integer stayMinutes = stations.get(i).getStayMinutes();
                if (stayMinutes != null && stayMinutes > 0) {
                    totalMinutes += stayMinutes;
                }
            }
        }
        
        return totalMinutes;
    }

    @Override
    public List<RouterStation> reverseStations(List<RouterStation> stations) {
        if (stations == null || stations.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 反转列表
        List<RouterStation> reversedList = IntStream.rangeClosed(1, stations.size())
            .mapToObj(i -> {
                RouterStation original = stations.get(stations.size() - i);
                RouterStation reversed = new RouterStation();
                reversed.setStationSeq(i); // 重新编号从1开始
                reversed.setStationId(original.getStationId());
                reversed.setStayMinutes(original.getStayMinutes());
                // timePrefixSum会在保存时重新计算
                return reversed;
            })
            .collect(Collectors.toList());
        
        return reversedList;
    }
    
    /**
     * 获取两个站点间的通行时间
     */
    private Double getTravelTimeBetweenStations(Integer stationAId, Integer stationBId) {
        // 确保 stationAId < stationBId
        Integer smallerId = Math.min(stationAId, stationBId);
        Integer largerId = Math.max(stationAId, stationBId);
        
        StationConnection result = stationConnectionMapper.selectByStationIds(smallerId, largerId);
        
        return result != null ? result.getTravelTimeMinutes() : null;
    }
    
    /**
     * 计算时间前缀和
     */
    private void calculateTimePrefixSum(List<RouterStation> stations) {
        if (stations == null || stations.isEmpty()) {
            return;
        }
        
        double currentTime = 0.0;
        stations.get(0).setTimePrefixSum(currentTime); // 起点时间为0
        
        for (int i = 1; i < stations.size(); i++) {
            Integer prevStationId = stations.get(i - 1).getStationId();
            Integer currStationId = stations.get(i).getStationId();
            
            // 获取上一站到当前站的通行时间
            Double travelTime = getTravelTimeBetweenStations(prevStationId, currStationId);
            if (travelTime == null) {
                throw new BusinessException(String.format(
                    "站点 %d 和站点 %d 之间没有联通关系", prevStationId, currStationId
                ));
            }
            
            // 累加通行时间和停留时间
            currentTime += travelTime;
            Integer stayMinutes = stations.get(i - 1).getStayMinutes();
            if (stayMinutes != null && stayMinutes > 0) {
                currentTime += stayMinutes;
            }
            
            stations.get(i).setTimePrefixSum(currentTime);
        }
    }
}
