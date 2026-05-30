// src/api/UserApi.js
import { post, get, put, del } from "@/request/request.js"

// 分页查询用户
export function getUserPage(params) {
    return post('/api/v1/g/user', params)
}

// 查询所有用户
export function getAllUsers() {
    return get('/api/v1/g/users')
}

// 管理员更新用户
export function updateUser(id, params) {
    return put(`/api/v1/admin/user/update`, params)
}

// 管理员删除用户（修正：改为DELETE请求）
export function deleteUser(id) {
    return del(`/api/v1/admin/user/${id}`)
}