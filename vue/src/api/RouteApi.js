import request from '@/request/request.js'

/**
 * 路线管理API
 */

// 查询指定路线的所有站点
export function getRouteStations(routerId) {
    return request({
        url: '/api/v1/route/stations',
        method: 'post',
        data: { routerId }
    })
}

// 创建或更新路线站点关联
export function saveRouteStations(data) {
    return request({
        url: '/api/v1/route/save',
        method: 'post',
        data
    })
}

// 删除路线
export function deleteRoute(routerId) {
    return request({
        url: '/api/v1/route/delete',
        method: 'post',
        data: { routerId }
    })
}

// 验证站点是否联通
export function validateConnection(stationAId, stationBId) {
    return request({
        url: '/api/v1/route/validate/connection',
        method: 'post',
        data: { stationAId, stationBId }
    })
}
