import { post, get } from "@/request/request.js"

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
    return post(`/api/v1/admin/user/${id}`, params)
}

// 管理员删除用户
export function deleteUser(id) {
    return post(`/api/v1/admin/user/${id}`)
}
