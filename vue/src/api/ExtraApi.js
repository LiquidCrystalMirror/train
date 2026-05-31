// src/api/ExtraApi.js
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

/**
 * 分页查询所有已售票（含车票详情、车次号、用户ID、票价）
 */
export function getSoldTickets(params) {
    return post('/api/v1/sale/sold/page', params)
}

/**
 * 根据车票ID查询站点详情（出发站、到达站、上车时间、到达时间）
 */
export function getStationDetailByTicket(ticketId) {
    return post('/api/v1/ticket/station-detail', { ticketId })
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

export function getRefundDetail(refundId) {
    return post('/api/v1/refund/detail', { refundId })
}

// ==================== 站点管理接口 ====================

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

// ==================== 价格管理接口（修正）====================

// 查询车次的价格梯度列表
export function getPriceList(trainId) {
    return post('/api/v1/price/list', { trainId })
}

// 设置单个价格梯度
export function setPrice(trainId, stationCount, price) {
    return post('/api/v1/price/set', { trainId, stationCount, price })
}

// 批量设置价格梯度
export function batchSetPrices(trainId, priceList) {
    return post('/api/v1/price/batch', { trainId, priceList })
}

// 检查价格梯度是否完整
export function checkPriceComplete(trainId, totalStationCount) {
    return post('/api/v1/price/check-complete', { trainId, totalStationCount })
}

// 计算票价（售前预估，按座位类型 + 站点序号计算）
// 参数：trainId, seatType（1=二等座, 2=一等座, 3=商务座）, startStationSeq, endStationSeq
export function calculatePrice(trainId, seatType, startStationSeq, endStationSeq) {
    return post('/api/v1/sale/calculate-price', { trainId, seatType, startStationSeq, endStationSeq })
}
