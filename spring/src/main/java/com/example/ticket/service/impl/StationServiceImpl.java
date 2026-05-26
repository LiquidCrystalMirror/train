package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.Station;
import com.example.ticket.entity.StationConnection;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.StationConnectionMapper;
import com.example.ticket.mapper.StationMapper;
import com.example.ticket.service.StationService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StationServiceImpl extends ServiceImpl<StationMapper, Station> implements StationService {

    @Resource
    private StationConnectionMapper connectionMapper;

    @Override
    public boolean addStation(Station station) {
        LambdaQueryWrapper<Station> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Station::getStationName, station.getStationName());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("站点名称已存在：" + station.getStationName());
        }
        return save(station);
    }

    @Override
    @Transactional
    public void addConnection(Integer stationAId, Integer stationBId, Double travelTimeMinutes) {
        if (baseMapper.selectById(stationAId) == null) {
            System.out.println("站点不存在，ID：" + stationAId);
            throw new BusinessException("站点不存在，ID：" + stationAId);
        }
        if (baseMapper.selectById(stationBId) == null) {
            System.out.println("站点不存在，ID：" + stationBId);
            throw new BusinessException("站点不存在，ID：" + stationBId);
        }

        if (isConnected(stationAId, stationBId)) {
            throw new BusinessException("站点已连通，不可重复添加");
        }

        int aId = stationAId;
        int bId = stationBId;
        if (aId > bId) {
            int tmp = aId;
            aId = bId;
            bId = tmp;
        }

        StationConnection connection = new StationConnection();
        connection.setStationAId(aId);
        connection.setStationBId(bId);
        connection.setTravelTimeMinutes(travelTimeMinutes);
        connectionMapper.insert(connection);
    }

    @Override
    @Transactional
    public boolean removeConnection(Integer stationAId, Integer stationBId) {
        int aId = stationAId;
        int bId = stationBId;
        if (aId > bId) {
            int tmp = aId;
            aId = bId;
            bId = tmp;
        }
        LambdaQueryWrapper<StationConnection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StationConnection::getStationAId, aId)
                .eq(StationConnection::getStationBId, bId);
        return connectionMapper.delete(wrapper) > 0;
    }



    @Override
    public List<Map<String, Object>> getNeighborStationsWithTime(Integer stationId) {
        List<Map<String, Object>> neighborsMap = connectionMapper.findNeighborsByStationId(stationId);
        if (neighborsMap.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        List<Integer> neighborIds = new ArrayList<>();
        
        for (Map<String, Object> map : neighborsMap) {
            Object idObj = map.get("neighbor_station_id");
            Integer neighborId = null;
            if (idObj instanceof Integer) {
                neighborId = (Integer) idObj;
            } else if (idObj instanceof Number) {
                neighborId = ((Number) idObj).intValue();
            }
            
            if (neighborId != null) {
                neighborIds.add(neighborId);
            }
        }
        
        if (!neighborIds.isEmpty()) {
            List<Station> stations = baseMapper.selectBatchIds(neighborIds);
            Map<Integer, Station> stationMap = new HashMap<>();
            for (Station station : stations) {
                stationMap.put(station.getStationId(), station);
            }
            
            for (Map<String, Object> map : neighborsMap) {
                Object idObj = map.get("neighbor_station_id");
                Integer neighborId = null;
                if (idObj instanceof Integer) {
                    neighborId = (Integer) idObj;
                } else if (idObj instanceof Number) {
                    neighborId = ((Number) idObj).intValue();
                }
                
                if (neighborId != null && stationMap.containsKey(neighborId)) {
                    Station station = stationMap.get(neighborId);
                    Map<String, Object> neighborInfo = new HashMap<>();
                    neighborInfo.put("neighborStationId", station.getStationId());
                    neighborInfo.put("neighborStationName", station.getStationName());
                    neighborInfo.put("travelTimeMinutes", map.get("travel_time_minutes"));
                    result.add(neighborInfo);
                }
            }
        }
        
        return result;
    }

    @Override
    public boolean isConnected(Integer stationAId, Integer stationBId) {
        int aId = stationAId;
        int bId = stationBId;
        if (aId > bId) {
            int tmp = aId;
            aId = bId;
            bId = tmp;
        }
        LambdaQueryWrapper<StationConnection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StationConnection::getStationAId, aId)
                .eq(StationConnection::getStationBId, bId);
        return connectionMapper.selectCount(wrapper) > 0;
    }
}