import {post} from "@/request/request.js"

export function registerApi(params) {
    return post('/api/v1/reg', params)
}