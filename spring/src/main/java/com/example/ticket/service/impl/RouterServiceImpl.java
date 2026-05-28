package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.Router;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.RouterMapper;
import com.example.ticket.service.RouterService;
import com.example.ticket.service.RouterStationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RouterServiceImpl extends ServiceImpl<RouterMapper, Router> implements RouterService {

    @Resource
    private RouterStationService routerStationService;


    @Override
    public List<Router> listAllRouters() {
        return this.list();
    }

    @Override
    public Router getRouterById(Integer routerId) {
        return this.getById(routerId);
    }

    @Override
    @Transactional
    public Integer createRouter(String routerName) {
        Router router = new Router();
        router.setRouterName(routerName);
        this.save(router);
        return router.getRouterId(); // 自增主键返回
    }

    @Override
    @Transactional
    public boolean deleteRouter(Integer routerId) {
        // 先删除关联的站点信息（如果外键未设级联删除，需手动删除）
        routerStationService.deleteByRouterId(routerId);
        // 再删除路线基本信息
        return this.removeById(routerId);
    }

    @Override
    @Transactional
    public boolean updateRouterName(Integer routerId, String routerName) {
        Router router = this.getById(routerId);
        if (router == null) {
            throw new BusinessException("路线不存在");
        }
        router.setRouterName(routerName);
        return this.updateById(router);
    }

    @Override
    public boolean updateTotalDuration(Integer routerId) {
        Double totalDuration = routerStationService.calculateTotalDuration(routerId);
        Router router = new Router();
        router.setRouterId(routerId);
        router.setTotalDuration(totalDuration);
        return this.updateById(router);
    }
}