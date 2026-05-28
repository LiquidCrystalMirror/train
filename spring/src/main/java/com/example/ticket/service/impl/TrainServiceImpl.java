package com.example.ticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ticket.entity.Router;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.exception.BusinessException;
import com.example.ticket.mapper.TrainInfoMapper;
import com.example.ticket.service.RouterService;
import com.example.ticket.service.TrainService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TrainServiceImpl extends ServiceImpl<TrainInfoMapper, TrainInfo> implements TrainService {

    @Resource
    private RouterService routerService;

    @Override
    public boolean save(TrainInfo trainInfo) {
        // 新增时根据路线ID自动设置总耗时
        if (trainInfo.getRouterId() != null) {
            Router router = routerService.getRouterById(trainInfo.getRouterId());
            if (router == null) {
                throw new BusinessException("选择的路线不存在");
            }
            Double totalDuration = router.getTotalDuration();
            if (totalDuration == null) {
                throw new BusinessException("所选路线尚未计算总耗时，请先维护路线站点信息");
            }
            // totalDuration 为分钟数（Double），转为 Integer 保存
            trainInfo.setTimeConsuming(totalDuration.intValue());
        }
        return super.save(trainInfo);
    }

    @Override
    public boolean updateById(TrainInfo trainInfo) {
        // 更新时若修改了路线，同样重新计算耗时
        if (trainInfo.getRouterId() != null) {
            Router router = routerService.getRouterById(trainInfo.getRouterId());
            if (router == null) {
                throw new BusinessException("选择的路线不存在");
            }
            Double totalDuration = router.getTotalDuration();
            if (totalDuration == null) {
                throw new BusinessException("所选路线尚未计算总耗时，请先维护路线站点信息");
            }
            trainInfo.setTimeConsuming(totalDuration.intValue());
        }
        return super.updateById(trainInfo);
    }
}