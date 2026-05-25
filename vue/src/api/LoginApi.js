import {post} from "@/request/request.js"

export function loginApi(params) {
    return post('/api/v1/login', params)
}