package com.example.ticket.util;

import java.security.SecureRandom;

/**
 * 简化版雪花ID生成器
 * 最后一位用于标识往返路线：0=往程，1=返程
 */
public class SimpleSnowflakeIdGenerator {
    
    private static final long EPOCH = 1700000000000L; // 自定义起始时间戳
    private static final int DIRECTION_BITS = 1; // 方向位占用1bit
    private static final long SEQUENCE_MASK = (1L << DIRECTION_BITS) - 1; // 方向掩码
    
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * 生成路线ID
     * @param baseId 基础ID（由系统生成的唯一ID）
     * @param isReturn 是否为返程路线
     * @return 路线ID，最后一位表示方向（0=往程，1=返程）
     */
    public static long generateRouteId(long baseId, boolean isReturn) {
        // 确保baseId的最后一位是0（偶数）
        long cleanBaseId = baseId & ~SEQUENCE_MASK;
        // 设置方向位
        return cleanBaseId | (isReturn ? 1L : 0L);
    }
    
    /**
     * 从路线ID中判断是否为返程
     * @param routeId 路线ID
     * @return true=返程，false=往程
     */
    public static boolean isReturnRoute(long routeId) {
        return (routeId & SEQUENCE_MASK) == 1L;
    }
    
    /**
     * 获取对应方向的路线ID
     * @param routeId 当前路线ID
     * @param isReturn 目标方向
     * @return 对应方向的路线ID
     */
    public static long getOppositeRouteId(long routeId, boolean isReturn) {
        long baseId = routeId & ~SEQUENCE_MASK;
        return baseId | (isReturn ? 1L : 0L);
    }
    
    /**
     * 生成一个基础ID（偶数）
     * @return 基础ID
     */
    public static long generateBaseId() {
        // 使用时间戳 + 随机数生成唯一ID
        long timestamp = System.currentTimeMillis() - EPOCH;
        long randomPart = random.nextInt(10000);
        long baseId = (timestamp << 14) | randomPart;
        // 确保是偶数
        return baseId & ~1L;
    }
}
