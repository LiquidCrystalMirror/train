import axios from 'axios';
import qs from 'qs'
import authService from '@/service/AuthService'

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
    
    // 检查后端返回的业务状态码
    if (res.code !== 200) {
        console.error('请求错误:', res.message || '未知错误');
        
        // 如果是认证错误，跳转到登录页
        if (res.code === 401) {
            authService.redirectToLogin();
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
            authService.redirectToLogin();
            return Promise.reject('登录已过期，请重新登录');
        } else if (status === 403) {
            return Promise.reject('权限不足');
        } else if (status === 404) {
            return Promise.reject('请求的资源不存在');
        } else if (status === 500) {
            return Promise.reject('服务器内部错误');
        } else {
            return Promise.reject('请求失败: ' + status);
        }
    }
    
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
