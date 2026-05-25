import { post } from "@/request/request.js"

// 查询订单列表（需要根据后端实际接口调整）
export function getOrderPage(params) {
    return post('/api/v1/sale/list', params)
}

// 查询退票记录（需要根据后端实际接口调整）
export function getRefundPage(params) {
    return post('/api/v1/refund/list', params)
}
