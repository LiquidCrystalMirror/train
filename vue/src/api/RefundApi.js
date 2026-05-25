import { post } from "@/request/request.js"

// 退票
export function refundTicket(saleId) {
    return post('/api/v1/refund/do', { saleId })
}
