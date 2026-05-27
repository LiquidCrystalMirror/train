import { post } from '@/request/request.js'

/**
 * 车次发车时间管理API
 */

// 查询指定列车的所有发车时间
export function getSchedules(trainId) {
    return post('/api/v1/departure/list', { trainId })
}

// 根据时间范围查询车次
export function queryByTimeRange(startTime, endTime) {
    return post('/api/v1/departure/query/timeRange', { startTime, endTime })
}

// 创建发车时间表
export function createSchedule(data) {
    return post('/api/v1/departure/create', data)
}

// 删除发车时间表
export function deleteSchedule(id) {
    return post('/api/v1/departure/delete', { id })
}
