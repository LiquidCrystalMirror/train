import { post } from '@/request/request.js'

/**
 * 路线管理API
 */

// 查询指定路线的所有站点
export function getRouteStations(routerId) {
    return post('/api/v1/route/stations', { routerId })
}

// 创建或更新路线站点关联
export function saveRouteStations(data) {
    return post('/api/v1/route/save', data)
}

// 删除路线
export function deleteRoute(routerId) {
    return post('/api/v1/route/delete', { routerId })
}

// 验证站点是否联通
export function validateConnection(stationAId, stationBId) {
    return post('/api/v1/route/validate/connection', { stationAId, stationBId })
}
