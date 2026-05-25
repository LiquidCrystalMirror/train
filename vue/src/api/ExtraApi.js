import { post, get } from "@/request/request.js"

// ==================== 统计接口 ====================

// 获取系统统计数据
export function getSystemStats() {
    return get('/api/v1/stats/system')
}

// 获取用户统计
export function getUserStats() {
    return get('/api/v1/stats/users')
}

// 获取车次统计
export function getTrainStats() {
    return get('/api/v1/stats/trains')
}

// 获取车票统计
export function getTicketStats() {
    return get('/api/v1/stats/tickets')
}

// 获取订单统计
export function getOrderStats() {
    return get('/api/v1/stats/orders')
}

// ==================== 用户订单接口 ====================

// 查询用户的购票记录
export function getUserPurchases(userId) {
    return get(`/api/v1/sale/user/${userId}`)
}

// 查询用户的退票记录
export function getUserRefunds(userId) {
    return get(`/api/v1/refund/user/${userId}`)
}

// 分页查询用户的购票记录
export function getUserPurchasePage(params) {
    return post('/api/v1/sale/user/page', params)
}

// 分页查询用户的退票记录
export function getUserRefundPage(params) {
    return post('/api/v1/refund/user/page', params)
}

// ==================== 售票列表接口 ====================

// 查询所有售票记录（管理员）
export function getAllSales(params) {
    return post('/api/v1/sale/list', params)
}

// 根据车次ID查询售票记录
export function getSalesByTrain(trainId) {
    return get(`/api/v1/sale/train/${trainId}`)
}

// 根据车票ID查询售票记录
export function getSalesByTicket(ticketId) {
    return get(`/api/v1/sale/ticket/${ticketId}`)
}

// ==================== 退票列表接口 ====================

// 查询所有退票记录（管理员）
export function getAllRefunds(params) {
    return post('/api/v1/refund/list', params)
}

// 根据售票记录ID查询退票记录
export function getRefundBySale(saleId) {
    return get(`/api/v1/refund/sale/${saleId}`)
}

// ==================== 站点管理接口（可选扩展）====================

// 查询所有站点
export function getAllStations() {
    return get('/api/v1/station/list')
}

// 添加站点
export function addStation(params) {
    return post('/api/v1/station/add', params)
}

// 更新站点
export function updateStation(params) {
    return post('/api/v1/station/update', params)
}

// 删除站点
export function deleteStation(id) {
    return post('/api/v1/station/delete', { id })
}

// ==================== 车次站点关联接口（可选扩展）====================

// 查询车次的站点序列
export function getTrainStations(trainId) {
    return get(`/api/v1/train/${trainId}/stations`)
}

// 设置车次的站点序列
export function setTrainStations(trainId, stations) {
    return post(`/api/v1/train/${trainId}/stations`, stations)
}

// ==================== 价格管理接口（可选扩展）====================

// 查询票价规则
export function getPriceRules() {
    return get('/api/v1/price/rules')
}

// 更新票价规则
export function updatePriceRules(params) {
    return post('/api/v1/price/update', params)
}

// 计算票价
export function calculatePrice(params) {
    return post('/api/v1/price/calculate', params)
}
