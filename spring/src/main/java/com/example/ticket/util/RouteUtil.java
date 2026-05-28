package com.example.ticket.util;

/**
 * 路线工具类 - 处理往返路线的识别和计算
 */
public class RouteUtil {

    /**
     * 判断是否为往程路线（ID最后一位为0，即偶数）
     * @param routerId 路线ID
     * @return true=往程，false=返程
     */
    public static boolean isForwardRoute(Long routerId) {
        if (routerId == null) {
            return false;
        }
        return (routerId & 1L) == 0L;
    }

    /**
     * 获取对应的返程路线ID
     * 如果当前是往程（偶数），返回 routerId + 1
     * 如果当前是返程（奇数），返回 routerId - 1
     * @param routerId 当前路线ID
     * @return 对应的反向路线ID
     */
    public static Long getOppositeRouteId(Long routerId) {
        if (routerId == null) {
            return null;
        }
        // 清除最后一位，然后加1得到反向路线
        long baseId = routerId & ~1L;
        return isForwardRoute(routerId) ? baseId + 1 : baseId;
    }

    /**
     * 获取基础ID（清除方向位）
     * @param routerId 路线ID
     * @return 基础ID（偶数）
     */
    public static Long getBaseId(Long routerId) {
        if (routerId == null) {
            return null;
        }
        return routerId & ~1L;
    }

    /**
     * 生成往程路线ID
     * @param baseId 基础ID（必须是偶数）
     * @return 往程路线ID
     */
    public static Long generateForwardRouteId(Long baseId) {
        if (baseId == null) {
            return null;
        }
        // 确保是偶数
        return baseId & ~1L;
    }

    /**
     * 生成返程路线ID
     * @param baseId 基础ID（必须是偶数）
     * @return 返程路线ID
     */
    public static Long generateReturnRouteId(Long baseId) {
        if (baseId == null) {
            return null;
        }
        // 确保是偶数后加1
        return (baseId & ~1L) + 1;
    }
}
