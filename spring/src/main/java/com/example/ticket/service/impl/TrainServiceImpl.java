package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.Router;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.TrainInfoMapper;
import com.example.ticket.service.RouterService;
import com.example.ticket.service.TrainService;
import com.example.ticket.util.RouteUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class TrainServiceImpl extends ServiceImpl<TrainInfoMapper, TrainInfo> implements TrainService {

    @Resource
    private RouterService routerService;
    
    @Resource
    private TrainInfoMapper trainInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer createTrainWithReturn(String trainNumber, Long routerId) {
        // 1. 参数验证
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            throw new BusinessException("列车编号不能为空");
        }
        if (routerId == null) {
            throw new BusinessException("路线ID不能为空");
        }
        
        // 2. 判断传入的路线是往程还是返程
        boolean isForward = RouteUtil.isForwardRoute(routerId);
        Long forwardRouteId = isForward ? routerId : RouteUtil.getOppositeRouteId(routerId);
        Long returnRouteId = RouteUtil.getOppositeRouteId(forwardRouteId);
        
        // 3. 验证往返路线是否存在
        Router forwardRouter = routerService.getRouterById(forwardRouteId);
        if (forwardRouter == null) {
            throw new BusinessException("往程路线不存在");
        }
        
        Router returnRouter = routerService.getRouterById(returnRouteId);
        if (returnRouter == null) {
            throw new BusinessException("返程路线不存在，请检查路线数据完整性");
        }
        
        // 4. 检查列车编号是否已存在
        TrainInfo existingTrain = this.lambdaQuery()
                .eq(TrainInfo::getTrainNumber, trainNumber)
                .one();
        if (existingTrain != null) {
            throw new BusinessException("列车编号已存在：" + trainNumber);
        }
        
        // 5. 计算总耗时（从往程路线获取）
        Double totalDuration = forwardRouter.getTotalDuration();
        if (totalDuration == null) {
            throw new BusinessException("所选路线尚未计算总耗时，请先维护路线站点信息");
        }
        Integer timeConsuming = totalDuration.intValue();
        
        // 6. 创建一趟列车（包含往返路线信息）
        TrainInfo train = new TrainInfo();
        train.setTrainNumber(trainNumber);
        train.setRouterId(forwardRouteId);  // 往程路线
        train.setOppsiteRouterId(returnRouteId);  // 返程路线
        train.setTimeConsuming(timeConsuming);
        this.save(train);
        
        return train.getTrainId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTrainWithReturn(Integer trainId, String trainNumber, Long routerId) {
        // 1. 查询当前列车
        TrainInfo currentTrain = this.getById(trainId);
        if (currentTrain == null) {
            throw new BusinessException("列车不存在");
        }
        
        // 2. 获取当前往返路线
        Long currentRouterId = currentTrain.getRouterId();
        Long oppositeRouterId = currentTrain.getOppsiteRouterId();
        
        if (currentRouterId == null || oppositeRouterId == null) {
            throw new BusinessException("列车路线信息不完整");
        }
        
        // 3. 更新列车编号（如果提供）
        if (trainNumber != null && !trainNumber.trim().isEmpty()) {
            // 检查新编号是否与其他列车冲突
            TrainInfo conflictTrain = this.lambdaQuery()
                    .eq(TrainInfo::getTrainNumber, trainNumber)
                    .ne(TrainInfo::getTrainId, trainId)
                    .one();
            if (conflictTrain != null) {
                throw new BusinessException("列车编号已存在：" + trainNumber);
            }
            
            currentTrain.setTrainNumber(trainNumber);
        }
        
        // 4. 更新路线（如果提供）
        if (routerId != null) {
            // 判断新路线方向
            boolean isNewForward = RouteUtil.isForwardRoute(routerId);
            Long newForwardRouteId = isNewForward ? routerId : RouteUtil.getOppositeRouteId(routerId);
            Long newReturnRouteId = RouteUtil.getOppositeRouteId(newForwardRouteId);
            
            // 验证往返路线是否存在
            Router newForwardRouter = routerService.getRouterById(newForwardRouteId);
            if (newForwardRouter == null) {
                throw new BusinessException("新往程路线不存在");
            }
            
            Router newReturnRouter = routerService.getRouterById(newReturnRouteId);
            if (newReturnRouter == null) {
                throw new BusinessException("新返程路线不存在");
            }
            
            // 获取新路线的总耗时
            Double newTotalDuration = newForwardRouter.getTotalDuration();
            if (newTotalDuration == null) {
                throw new BusinessException("新路线尚未计算总耗时");
            }
            Integer newTimeConsuming = newTotalDuration.intValue();
            
            // 更新列车的往返路线
            currentTrain.setRouterId(newForwardRouteId);
            currentTrain.setOppsiteRouterId(newReturnRouteId);
            currentTrain.setTimeConsuming(newTimeConsuming);
        }
        
        // 5. 保存更新
        this.updateById(currentTrain);
        
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTrainWithReturn(Integer trainId) {
        // 1. 查询当前列车
        TrainInfo currentTrain = this.getById(trainId);
        if (currentTrain == null) {
            throw new BusinessException("列车不存在");
        }
        
        // 2. 直接删除列车（往返路线信息都在这一条记录中）
        this.removeById(trainId);
        
        return true;
    }
}
