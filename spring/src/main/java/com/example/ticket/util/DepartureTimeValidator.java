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
     * 验证发车时间是否在允许范围内（基于当前时间，不考虑水位表）
     * @deprecated 请使用 {@link #isValidDepartureTime(LocalDateTime, TrainScheduleWatermark)}
     * @param departureTime 请求的发车时间
     * @return true=合法，false=超出范围
     */
    @Deprecated
    public static boolean isValidDepartureTime(LocalDateTime departureTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxTime = now.plusHours(24);
        return departureTime.isAfter(now) && !departureTime.isAfter(maxTime);
    }

    /**
     * 验证发车时间是否在允许范围内（基于水位表与服务器时间的基准）
     * @param departureTime 请求的发车时间
     * @param watermark 水位表记录（可为null）
     * @return true=合法，false=不合法
     */
    public static boolean isValidDepartureTime(LocalDateTime departureTime, TrainScheduleWatermark watermark) {
        LocalDateTime baseTime = getBaseTime(watermark);
        LocalDateTime maxTime = baseTime.plusHours(24);
        // 发车时间必须 >= 基准时间，且 <= 基准时间+24小时
        return !departureTime.isBefore(baseTime) && !departureTime.isAfter(maxTime);
    }

    /**
     * 获取基准时间（服务器时间与水位表到达时间的较大值）
     * @param watermark 水位表记录
     * @return 基准时间 = max(当前时间, 水位表到达时间)，若水位表为null则返回当前时间
     */
    public static LocalDateTime getBaseTime(TrainScheduleWatermark watermark) {
        LocalDateTime now = LocalDateTime.now();
        if (watermark != null && watermark.getArriveTime() != null) {
            // 水位表在未来，用水位表；水位表在过去，用当前时间
            return watermark.getArriveTime().isAfter(now) ? watermark.getArriveTime() : now;
        }
        return now;
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
     * @param args 参数（如基准时间）
     * @return 错误消息
     */
    public static String buildErrorMessage(ErrorType type, Object... args) {
        switch (type) {
            case TIME_OUT_OF_RANGE:
                return "发车时间必须在基准时间往后24小时内";
            case DIRECTION_NOT_ALTERNATING:
                return "发车方向必须与上一次交替（往→返→往）";
            case TIME_BEFORE_BASE:
                return String.format("发车时间必须在基准时间(%s)之后", args[0]);
            default:
                return "未知错误";
        }
    }

    public enum ErrorType {
        TIME_OUT_OF_RANGE,
        DIRECTION_NOT_ALTERNATING,
        TIME_BEFORE_BASE
    }
}