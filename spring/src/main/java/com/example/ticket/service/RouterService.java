package com.example.ticket.service;

import com.example.ticket.entity.Router;

import java.util.List;

public interface RouterService {
    List<Router> listAllRouters();
    Router getRouterById(Integer routerId);
    Integer createRouter(String routerName);
    boolean updateRouterName(Integer routerId, String routerName);
    boolean deleteRouter(Integer routerId);

    // 更新总时长（内部调用）
    boolean updateTotalDuration(Integer routerId);
}