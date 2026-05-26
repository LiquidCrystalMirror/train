package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.Station;
import com.example.ticket.exception.BusinessException;

import java.util.List;
import java.util.Map;

public interface StationService extends IService<Station> {
    boolean addStation(Station station);

    void addConnection(Integer stationAId, Integer stationBId, Double travelTimeMinutes);

    boolean removeConnection(Integer stationAId, Integer stationBId);


    List<Map<String, Object>> getNeighborStationsWithTime(Integer stationId);

    boolean isConnected(Integer stationAId, Integer stationBId);
}
