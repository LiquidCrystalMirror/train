import axios from 'axios';
import qs from 'qs'
import authService from '@/service/AuthService'
import { ElMessage } from 'element-plus'

const instance = axios.create({
});
instance.defaults.headers.post['Content-Type'] = 'application/json';


const whiteList = ['/login', '/register']

instance.interceptors.request.use(function (config) {
    const isWhitePath = whiteList.some(path => config.url.includes(path))

    if (isWhitePath) {
        return config
    }

    if (!authService.isTokenValid()) {
        authService.redirectToLogin()
        return Promise.reject(new Error('登录已过期'))
    }

    const token = authService.getToken()
    if (token) {
        config.headers['Authorization'] = `Bearer ${token}`
    }

    return config
}, function (error) {
    return Promise.reject(error)
})



instance.interceptors.response.use(function (response) {
    const res = response.data;
    
    // 调试日志：打印响应数据
    console.log('API响应:', res);
    
    // 检查返回的数据结构
    if (!res || typeof res !== 'object') {
        console.error('响应数据格式错误:', res);
        return Promise.reject('响应数据格式错误');
    }
    
    // 检查后端返回的业务状态码
    if (res.code === undefined || res.code === null) {
        console.error('响应缺少code字段:', res);
        return Promise.reject('响应数据格式错误：缺少code字段');
    }
    
    if (res.code !== 200) {
        console.error('请求错误:', res.message || '未知错误', 'Code:', res.code);
        
        // 如果是认证错误（401），显示提示并跳转到登录页
        if (res.code === 401) {
            ElMessage.warning(res.message || '登录已过期，请重新登录');
            // 延迟跳转，让用户看到提示
            setTimeout(() => {
                authService.redirectToLogin();
            }, 500);
        }
        
        return Promise.reject(res.message || '请求失败');
    }
    
    // 成功时返回完整的ApiResult对象，包含code、message、data
    return res;
}, function (error) {
    console.log(error)
    
    // HTTP 状态码错误处理
    if (error.response) {
        const status = error.response.status;
        
        if (status === 401) {
            ElMessage.warning('登录已过期，请重新登录');
            setTimeout(() => {
                authService.redirectToLogin();
            }, 500);
            return Promise.reject('登录已过期，请重新登录');
        } else if (status === 403) {
            ElMessage.error('权限不足');
            return Promise.reject('权限不足');
        } else if (status === 404) {
            ElMessage.error('请求的资源不存在');
            return Promise.reject('请求的资源不存在');
        } else if (status === 500) {
            ElMessage.error('服务器内部错误');
            return Promise.reject('服务器内部错误');
        } else {
            ElMessage.error('请求失败: ' + status);
            return Promise.reject('请求失败: ' + status);
        }
    }
    
    ElMessage.error('服务器异常，请检查网络连接');
    return Promise.reject("服务器异常");
});

export function post(url, data = {}) {
    return new Promise((resolve, reject) => {
        instance.post(url, data).then(
            response => {
                resolve(response)
            },
            err => {
                reject(err)
            }
        )
    })
}

export function get(url, params = {}) {
    return new Promise((resolve, reject) => {
        instance
            .get(url, {
                params: params
            })
            .then(response => {
                resolve(response)
            })
            .catch(err => {
                reject(err)
            })
    })
}

export function put(url, data = {}) {
    return new Promise((resolve, reject) => {
        instance.put(url, data).then(
            response => {
                resolve(response)
            },
            err => {
                reject(err)
            }
        )
    })
}

export function del(url, params = {}) {
    return new Promise((resolve, reject) => {
        instance.delete(url, {
            params: params
        }).then(
            response => {
                resolve(response)
            },
            err => {
                reject(err)
            }
        )
    })
}
