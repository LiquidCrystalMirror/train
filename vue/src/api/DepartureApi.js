import request from '@/request/request.js'

/**
 * 车次发车时间管理API
 */

// 查询指定列车的所有发车时间
export function getSchedules(trainId) {
    return request({
        url: '/api/v1/departure/list',
        method: 'post',
        data: { trainId }
    })
}

// 根据时间范围查询车次
export function queryByTimeRange(startTime, endTime) {
    return request({
        url: '/api/v1/departure/query/timeRange',
        method: 'post',
        data: { startTime, endTime }
    })
}

// 创建发车时间表
export function createSchedule(data) {
    return request({
        url: '/api/v1/departure/create',
        method: 'post',
        data
    })
}

// 删除发车时间表
export function deleteSchedule(id) {
    return request({
        url: '/api/v1/departure/delete',
        method: 'post',
        data: { id }
    })
}
