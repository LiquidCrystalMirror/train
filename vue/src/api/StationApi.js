import { get, post, put, del } from '@/request/request'

const BASE_URL = '/api/v1/station'

export default {
    addStation(station) {
        return post(`${BASE_URL}/add`, station)
    },

    deleteStation(id) {
        return del(`${BASE_URL}/${id}`)
    },

    updateStation(station) {
        return put(`${BASE_URL}/update`, station)
    },

    listStations() {
        return get(`${BASE_URL}/list`)
    },

    queryById(id) {
        return get(`${BASE_URL}/${id}`)
    },

    queryByName(name) {
        return get(`${BASE_URL}/search`, { name })
    },

    addConnection(stationAId, stationBId, travelTimeMinutes) {
        return post(`${BASE_URL}/connection/add`, {
            stationAId,
            stationBId,
            travelTimeMinutes
        })
    },

    removeConnection(stationAId, stationBId) {
        return del(`${BASE_URL}/connection/${stationAId}/${stationBId}`)
    },

    getNeighbors(stationId) {
        return get(`${BASE_URL}/${stationId}/neighbors`)
    },

    checkConnection(stationAId, stationBId) {
        return get(`${BASE_URL}/check/connection/${stationAId}/${stationBId}`)
    }
}

// 导出单独的函数供RouteView使用
export function getAllStations() {
    return get(`${BASE_URL}/list`)
}
