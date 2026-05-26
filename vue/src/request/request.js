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
    return response.data;
}, function (error) {
    console.log(error)
    
    if (error.response && error.response.status === 401) {
        authService.redirectToLogin()
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
