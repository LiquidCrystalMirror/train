// src/api/RouteApi.js
import { post, get } from '@/request/request.js'

// 查询指定路线的所有站点（修改为GET请求）
export function getRouteStations(routerId) {
    return get('/api/v1/route/stations', { routerId })
}

// 创建或更新路线站点关联（后端不存在，建议删除或注释）
// export function saveRouteStations(data) {
//     return post('/api/v1/route/save', data)
// }

// 删除路线
export function deleteRoute(routerId) {
    return post('/api/v1/route/delete', { routerId })
}

// 验证站点是否联通（后端不存在，建议删除或注释）
// export function validateConnection(stationAId, stationBId) {
//     return post('/api/v1/route/validate/connection', { stationAId, stationBId })
// }

// ========== 新增接口 ==========
// 获取所有路线列表（仅基本信息）
export function getRouteList() {
    return get('/api/v1/route/list')
}

// 获取路线详情（基本信息 + 站点列表）
export function getRouteDetail(routerId) {
    return get('/api/v1/route/detail', { routerId })
}

// 创建路线（基本信息 + 站点列表）
export function createRoute(data) {
    return post('/api/v1/route/create', data)
}

// 更新路线（可更新名称和站点列表）
export function updateRoute(data) {
    return post('/api/v1/route/update', data)
}