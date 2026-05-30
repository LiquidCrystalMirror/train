// src/api/WatermarkApi.js
import { post } from '@/request/request.js'

/**
 * 水位表管理API
 */

// 查询指定列车的最新水位记录
export function getLatestWatermark(trainId) {
    return post('/api/v1/watermark/latest', { trainId })
}

// 检查指定时间段是否在危险时间内
export function checkDangerZone(trainId, startTime, endTime) {
    return post('/api/v1/watermark/check/danger', { trainId, startTime, endTime })
}

// 获取可以出票的最早时间
export function getEarliestTicketTime(trainId) {
    return post('/api/v1/watermark/earliest/ticket/time', { trainId })
}

// 更新水位记录（管理员在发行新车票后调用）
export function updateWatermark(trainId, routeId, departTime, arriveTime, updatedBy) {
    return post('/api/v1/watermark/update', {
        trainId,
        routeId,
        departTime,
        arriveTime,
        updatedBy
    })
}