import { post } from "@/request/request.js"

// 分页查询车次
export function getTrainPage(params) {
    return post('/api/v1/train/list', params)
}

// 获取列车列表(不分页)
export function getTrainList(params) {
    return post('/api/v1/train/list', params)
}

// 添加车次
export function addTrain(params) {
    return post('/api/v1/train/add', params)
}

// 更新车次
export function updateTrain(params) {
    return post('/api/v1/train/update', params)
}

// 删除车次
export function deleteTrain(id) {
    return post('/api/v1/train/delete', { id })
}

// 按车次号查询
export function queryByNumber(number) {
    return post('/api/v1/train/query/number', { number })
}

// 按起止站点查询
export function queryByStations(startStationId, endStationId) {
    return post('/api/v1/train/query/stations', { startStationId, endStationId })
}

// 按时间范围查询
export function queryByTimeRange(startTime, endTime) {
    return post('/api/v1/train/query/timeRange', { startTime, endTime })
}
