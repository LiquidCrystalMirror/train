package com.example.ticket.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ticket.entity.TrainInfo;

public interface TrainService extends IService<TrainInfo> {
    
    /**
     * 新建列车（自动创建往返列车）
     * @param trainNumber 列车编号
     * @param routerId 路线ID（往程或返程均可）
     * @return 往程列车ID
     */
    Integer createTrainWithReturn(String trainNumber, Long routerId);
    
    /**
     * 修改列车信息（同步修改往返列车）
     * @param trainId 列车ID
     * @param trainNumber 列车编号（可选）
     * @param routerId 路线ID（可选）
     * @return 是否成功
     */
    boolean updateTrainWithReturn(Integer trainId, String trainNumber, Long routerId);
    
    /**
     * 删除列车（同时删除对应的往返列车）
     * @param trainId 列车ID
     * @return 是否成功
     */
    boolean deleteTrainWithReturn(Integer trainId);
}