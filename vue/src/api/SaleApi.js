import { post } from "@/request/request.js"

// 售票
export function sellTicket(params) {
    return post('/api/v1/sale/do', params)
}
