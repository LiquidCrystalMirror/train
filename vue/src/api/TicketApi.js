// src/api/TicketApi.js
import { post } from "@/request/request.js"

// 分页查询车票
export function getTicketPage(params) {
    return post('/api/v1/ticket/list', params)
}

// 根据车次ID查询车票
export function getTicketsByTrain(trainId) {
    return post('/api/v1/ticket/train', { trainId })
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