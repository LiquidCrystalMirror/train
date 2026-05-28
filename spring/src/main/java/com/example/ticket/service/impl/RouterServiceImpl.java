package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.Router;
import com.example.ticket.entity.RouterStation;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.RouterMapper;
import com.example.ticket.service.RouterService;
import com.example.ticket.service.RouterStationService;
import com.example.ticket.util.SimpleSnowflakeIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouterServiceImpl extends ServiceImpl<RouterMapper, Router> implements RouterService {

    @Resource
    private RouterStationService routerStationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRouteWithReturn(String routerName, List<Map<String, Object>> stationsData) {
        if (routerName == null || routerName.trim().isEmpty()) {
            throw new BusinessException("路线名称不能为空");
        }
        if (stationsData == null || stationsData.isEmpty()) {
            throw new BusinessException("站点列表不能为空");
        }
        
        // 1. 生成基础ID（偶数）
        long baseId = SimpleSnowflakeIdGenerator.generateBaseId();
        Long forwardRouteId = SimpleSnowflakeIdGenerator.generateRouteId(baseId, false);
        Long returnRouteId = SimpleSnowflakeIdGenerator.generateRouteId(baseId, true);
        
        // 2. 创建往程路线
        Router forwardRouter = new Router();
        forwardRouter.setRouterId(forwardRouteId);
        forwardRouter.setRouterName(routerName + "（往）");
        this.save(forwardRouter);
        
        // 3. 保存往程站点
        List<RouterStation> forwardStations = convertToRouterStations(stationsData);
        routerStationService.saveRouterStations(forwardRouteId, forwardStations);
        
        // 4. 计算并更新往程总时长
        updateTotalDuration(forwardRouteId);
        
        // 5. 创建返程路线
        Router returnRouter = new Router();
        returnRouter.setRouterId(returnRouteId);
        returnRouter.setRouterName(routerName + "（返）");
        this.save(returnRouter);
        
        // 6. 生成返程站点（反转顺序）
        List<RouterStation> returnStations = routerStationService.reverseStations(forwardStations);
        routerStationService.saveRouterStations(returnRouteId, returnStations);
        
        // 7. 计算并更新返程总时长
        updateTotalDuration(returnRouteId);
        
        return forwardRouteId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRoute(Long routerId, String routerName, List<Map<String, Object>> stationsData) {
        Router router = this.getById(routerId);
        if (router == null) {
            throw new BusinessException("路线不存在");
        }
        
        // 更新名称
        if (routerName != null && !routerName.trim().isEmpty()) {
            router.setRouterName(routerName);
            this.updateById(router);
        }
        
        // 更新站点
        if (stationsData != null && !stationsData.isEmpty()) {
            List<RouterStation> stations = convertToRouterStations(stationsData);
            routerStationService.saveRouterStations(routerId, stations);
            updateTotalDuration(routerId);
        }
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRoute(Long routerId) {
        Router router = this.getById(routerId);
        if (router == null) {
            throw new BusinessException("路线不存在");
        }
        
        // 获取基础ID
        long baseId = routerId & ~1L;
        Long returnRouteId = baseId + 1;
        
        // 删除往返路线的站点
        routerStationService.deleteByRouterId(routerId);
        routerStationService.deleteByRouterId(returnRouteId);
        
        // 删除往返路线
        this.removeById(routerId);
        this.removeById(returnRouteId);
        
        return true;
    }

    @Override
    public List<Router> listAllRouters() {
        return this.list();
    }

    @Override
    public Router getRouterById(Long routerId) {
        return this.getById(routerId);
    }

    @Override
    public List<RouterStation> getStationsByRouterId(Long routerId) {
        return routerStationService.getStationsByRouterId(routerId);
    }

    @Override
    public Map<String, Router> getRoutePair(Long routerId) {
        Router router = this.getById(routerId);
        if (router == null) {
            throw new BusinessException("路线不存在");
        }
        
        long baseId = routerId & ~1L;
        Long forwardRouteId = baseId;
        Long returnRouteId = baseId + 1;
        
        Router forwardRouter = this.getById(forwardRouteId);
        Router returnRouter = this.getById(returnRouteId);
        
        Map<String, Router> result = new HashMap<>();
        result.put("forwardRoute", forwardRouter);
        result.put("returnRoute", returnRouter);
        
        return result;
    }

    @Override
    public boolean updateTotalDuration(Long routerId) {
        Double totalDuration = routerStationService.calculateTotalDuration(routerId);
        Router router = new Router();
        router.setRouterId(routerId);
        router.setTotalDuration(totalDuration);
        return this.updateById(router);
    }
    
    /**
     * 转换前端数据为RouterStation对象
     */
    private List<RouterStation> convertToRouterStations(List<Map<String, Object>> stationsData) {
        return stationsData.stream().map(data -> {
            RouterStation rs = new RouterStation();
            rs.setStationSeq(((Number) data.get("stationSeq")).intValue());
            rs.setStationId(((Number) data.get("stationId")).intValue());
            rs.setStayMinutes(data.get("stayMinutes") != null ? 
                    ((Number) data.get("stayMinutes")).intValue() : 0);
            return rs;
        }).collect(Collectors.toList());
    }
}
