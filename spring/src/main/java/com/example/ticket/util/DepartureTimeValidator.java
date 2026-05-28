package com.example.ticket.util;

import com.example.ticket.entity.Router;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.entity.TrainScheduleWatermark;

import java.time.LocalDateTime;

/**
 * 发车时间验证工具类
 */
public class DepartureTimeValidator {

    /**
     * 验证发车时间是否在基准时间往后24小时内
     * @param departureTime 请求的发车时间
     * @param baseTime 基准时间
     * @return true=合法，false=超出范围
     */
    public static boolean isValidDepartureTime(LocalDateTime departureTime, LocalDateTime baseTime) {
        LocalDateTime maxTime = baseTime.plusHours(24);
        
        // 发车时间必须在基准时间之后，且在24小时内
        return departureTime.isAfter(baseTime) && !departureTime.isAfter(maxTime);
    }

    /**
     * 获取基准时间
     * 规则：如果水位记录的到达时间已过时（早于当前时间），则用当前时间
     *       如果水位记录的到达时间在未来，则用到达时间
     *       如果没有水位记录，用当前时间
     * @param watermark 水位表记录
     * @return 基准时间
     */
    public static LocalDateTime getBaseTime(TrainScheduleWatermark watermark) {
        if (watermark != null && watermark.getArriveTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            // 如果到达时间已过时，以当前时间为基准
            if (watermark.getArriveTime().isBefore(now)) {
                return now;
            }
            return watermark.getArriveTime();
        }
        return LocalDateTime.now();
    }

    /**
     * 检查是否满足交替方向规则
     * @param lastRouterId 上一次的路线ID
     * @param newRouterId 新的路线ID
     * @return true=满足交替规则，false=不满足
     */
    public static boolean isAlternatingDirection(Long lastRouterId, Long newRouterId) {
        if (lastRouterId == null) {
            // 第一次创建，任意方向都可以
            return true;
        }
        
        // 判断方向：偶数=往程，奇数=返程
        boolean lastIsForward = RouteUtil.isForwardRoute(lastRouterId);
        boolean newIsForward = RouteUtil.isForwardRoute(newRouterId);
        
        // 必须交替：上一次是往程，这次必须是返程；反之亦然
        return lastIsForward != newIsForward;
    }

    /**
     * 计算到达时间
     * @param departureTime 发车时间
     * @param router 路线信息
     * @return 到达时间 = 发车时间 + 路线总时长
     */
    public static LocalDateTime calculateArrivalTime(LocalDateTime departureTime, Router router) {
        if (router == null || router.getTotalDuration() == null) {
            throw new IllegalArgumentException("路线信息或总时长不能为空");
        }
        
        // totalDuration 单位是分钟
        return departureTime.plusMinutes(router.getTotalDuration().longValue());
    }

    /**
     * 构建错误消息
     * @param type 错误类型
     * @return 错误消息
     */
    public static String buildErrorMessage(ErrorType type, Object... args) {
        switch (type) {
            case TIME_OUT_OF_RANGE:
                return String.format("发车时间必须在基准时间(%s)往后24小时内", args[0]);
            case DIRECTION_NOT_ALTERNATING:
                return "发车方向必须与上一次交替（往→返→往）";
            default:
                return "未知错误";
        }
    }

    public enum ErrorType {
        TIME_OUT_OF_RANGE,
        DIRECTION_NOT_ALTERNATING
    }
}
