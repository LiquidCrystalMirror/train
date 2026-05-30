// src/api/TicketApi.js
import { post } from "@/request/request.js"

// 分页查询车票
export function getTicketPage(params) {
    return post('/api/v1/ticket/list', params)
}

// 根据车次ID查询车票
// @deprecated 请使用 getTicketInventory 代替
export function getTicketsByTrain(trainId) {
    return post('/api/v1/ticket/train', { trainId })
}

// 查询库存（按车次+发车时间，返回按座位类型聚合的库存）
export function getTicketInventory(trainId, departureTime) {
    return post('/api/v1/ticket/inventory', { trainId, departureTime })
}

// 以下接口后端已注释，暂时不可用
// 添加车票
// export function addTicket(params) {
//     return post('/api/v1/ticket/add', params)
// }

// 更新车票
// export function updateTicket(params) {
//     return post('/api/v1/ticket/update', params)
// }

// 删除车票
// export function deleteTicket(id) {
//     return post('/api/v1/ticket/delete', { id })
// }

// 批量生成车票（新增）
export function batchGenerateTickets(params) {
    return post('/api/v1/ticket/batch/generate', params)
}