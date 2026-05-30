package com.example.ticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticket.entity.*;
import com.example.ticket.enums.SeatTypeEnum;
import com.example.ticket.mapper.*;
import com.example.ticket.util.ApiResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 票务查询Controller — 提供按票查站点、按车次查路线等聚合查询能力
 */
@RestController
@RequestMapping("/api/v1/ticket")
public class TicketQueryController {

    @Resource
    private TicketInfoMapper ticketInfoMapper;

    @Resource
    private SaleInfoMapper saleInfoMapper;

    @Resource
    private TrainInfoMapper trainInfoMapper;

    @Resource
    private RouterStationMapper routerStationMapper;

    @Resource
    private RouterMapper routerMapper;

    @Resource
    private StationMapper stationMapper;

    // ==================== 1. 根据车票ID查上下车站详情 ====================

    /**
     * 根据 ticketId 查询该票对应的上下车站信息
     * 请求体：{ "ticketId": 1 }
     * 返回：车次、发车时间、座位类型、上下车站名称及序号
     */
    @PostMapping("/station-detail")
    public ApiResult<Map<String, Object>> getStationDetailByTicket(@RequestBody Map<String, Object> params) {
        Integer ticketId = params.get("ticketId") != null
                ? ((Number) params.get("ticketId")).intValue() : null;
        if (ticketId == null) {
            return ApiResult.error(400, "车票ID不能为空");
        }

        // 1. 查车票信息
        TicketInfo ticket = ticketInfoMapper.selectById(ticketId);
        if (ticket == null) {
            return ApiResult.error(404, "车票不存在");
        }

        // 2. 查售票记录（通过 ticketId + 已出票状态）
        List<SaleInfo> sales = saleInfoMapper.selectList(
                new LambdaQueryWrapper<SaleInfo>()
                        .eq(SaleInfo::getTicketId, ticketId)
                        .eq(SaleInfo::getSaleStatus, "已出票")
        );
        if (sales.isEmpty()) {
            return ApiResult.error(404, "未找到该车票的售票记录");
        }
        SaleInfo sale = sales.get(0);

        // 3. 查车次信息（获取车次号 + routerId）
        TrainInfo train = trainInfoMapper.selectById(ticket.getTrainId());
        if (train == null) {
            return ApiResult.error(404, "车次不存在");
        }

        // 4. 查路线站点列表
        List<RouterStation> routeStations = routerStationMapper.selectByRouterId(train.getRouterId());

        // 5. 匹配上下车序号对应的站点
        Integer startStationId = null;
        Integer endStationId = null;
        for (RouterStation rs : routeStations) {
            if (rs.getStationSeq().equals(sale.getStartStationSeq())) {
                startStationId = rs.getStationId();
            }
            if (rs.getStationSeq().equals(sale.getEndStationSeq())) {
                endStationId = rs.getStationId();
            }
        }

        // 6. 查站点名称
        Station startStation = startStationId != null ? stationMapper.selectById(startStationId) : null;
        Station endStation = endStationId != null ? stationMapper.selectById(endStationId) : null;

        // 7. 组装结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ticketId", ticket.getTicketId());
        result.put("trainId", train.getTrainId());
        result.put("trainNumber", train.getTrainNumber());
        result.put("departureTime", ticket.getDepartureTime() != null
                ? ticket.getDepartureTime().toString() : null);
        result.put("seatType", ticket.getSeatType());
        result.put("seatTypeName", SeatTypeEnum.getDescriptionByCode(ticket.getSeatType()));

        Map<String, Object> startInfo = new LinkedHashMap<>();
        startInfo.put("stationId", startStationId);
        startInfo.put("stationName", startStation != null ? startStation.getStationName() : null);
        startInfo.put("seq", sale.getStartStationSeq());
        result.put("startStation", startInfo);

        Map<String, Object> endInfo = new LinkedHashMap<>();
        endInfo.put("stationId", endStationId);
        endInfo.put("stationName", endStation != null ? endStation.getStationName() : null);
        endInfo.put("seq", sale.getEndStationSeq());
        result.put("endStation", endInfo);

        result.put("carriageNumber", ticket.getCarriageNumber());
        result.put("seatNumber", ticket.getSeatNumber());
        result.put("ticketStatus", ticket.getTicketStatus());

        return ApiResult.success("查询成功", result);
    }

    // ==================== 2. 根据车次ID查路线（含站点名称） ====================

    /**
     * 根据 trainId 查询该车次对应的完整路线信息（含站点名称）
     * 请求体：{ "trainId": 1 }
     * 返回：路线基本信息 + 按序号排列的站点列表（含站点名称）
     */
    @PostMapping("/train-route")
    public ApiResult<Map<String, Object>> getRouteByTrain(@RequestBody Map<String, Object> params) {
        Integer trainId = params.get("trainId") != null
                ? ((Number) params.get("trainId")).intValue() : null;
        if (trainId == null) {
            return ApiResult.error(400, "车次ID不能为空");
        }

        // 1. 查车次，获取 routerId
        TrainInfo train = trainInfoMapper.selectById(trainId);
        if (train == null) {
            return ApiResult.error(404, "车次不存在");
        }
        Long routerId = train.getRouterId();
        if (routerId == null) {
            return ApiResult.error(404, "该车次未关联路线");
        }

        // 2. 查路线基本信息
        Router router = routerMapper.selectById(routerId);

        // 3. 查路线站点列表（已按序号排序）
        List<RouterStation> routeStations = routerStationMapper.selectByRouterId(routerId);

        // 4. 批量查站点名称
        Map<Integer, String> stationNameMap = new HashMap<>();
        for (RouterStation rs : routeStations) {
            if (!stationNameMap.containsKey(rs.getStationId())) {
                Station station = stationMapper.selectById(rs.getStationId());
                stationNameMap.put(rs.getStationId(),
                        station != null ? station.getStationName() : "未知站点");
            }
        }

        // 5. 组装路线站点列表（含站点名称）
        List<Map<String, Object>> stations = new ArrayList<>();
        for (RouterStation rs : routeStations) {
            Map<String, Object> stationInfo = new LinkedHashMap<>();
            stationInfo.put("stationId", rs.getStationId());
            stationInfo.put("stationName", stationNameMap.getOrDefault(rs.getStationId(), "未知站点"));
            stationInfo.put("seq", rs.getStationSeq());
            stationInfo.put("stayMinutes", rs.getStayMinutes());
            stations.add(stationInfo);
        }

        // 6. 组装结果
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("trainId", train.getTrainId());
        result.put("trainNumber", train.getTrainNumber());
        result.put("routerId", routerId);
        result.put("routerName", router != null ? router.getRouterName() : null);
        result.put("isForward", routerId != null && (routerId & 1L) == 0L);
        result.put("stations", stations);

        return ApiResult.success("查询成功", result);
    }
}
