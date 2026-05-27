package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.CarriageInfo;

import java.util.List;

/**
 * 车厢信息模板服务接口
 */
public interface CarriageInfoService extends IService<CarriageInfo> {
    
    /**
     * 查询所有车厢模板
     */
    List<CarriageInfo> getAllTemplates();
    
    /**
     * 批量保存车厢模板
     */
    boolean saveTemplates(List<CarriageInfo> templates);
}
