package com.example.ticket.controller;

import com.example.ticket.entity.PriceSchedule;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.mapper.TrainInfoMapper;
import com.example.ticket.service.PriceScheduleService;
import com.example.ticket.util.ApiResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/price")
public class PriceScheduleController {

    @Resource
    private PriceScheduleService priceScheduleService;

    @Resource
    private TrainInfoMapper trainInfoMapper;

    /**
     * 验证车次是否存在
     */
    private ApiResult<Void> validateTrainExists(Integer trainId) {
        TrainInfo train = trainInfoMapper.selectById(trainId);
        if (train == null) {
            return ApiResult.error(404, "车次不存在，trainId: " + trainId);
        }
        return null;
    }

    /**
     * 验证车次是否已设置过价格梯度
     */
    private ApiResult<Void> validatePriceNotSet(Integer trainId) {
        List<PriceSchedule> existingPrices = priceScheduleService.getByTrainId(trainId);
        if (existingPrices != null && !existingPrices.isEmpty()) {
            return ApiResult.error(400, "该车次已设置过价格梯度，请使用更新接口");
        }
        return null;
    }

    @PostMapping("/list")
    public ApiResult<List<PriceSchedule>> listByTrain(@RequestBody Map<String, Integer> params) {
        Integer trainId = params.get("trainId");
        if (trainId == null) {
            return ApiResult.error(400, "车次ID不能为空");
        }
        List<PriceSchedule> prices = priceScheduleService.getByTrainId(trainId);
        return ApiResult.success("查询成功", prices);
    }

    @PostMapping("/set")
    public ApiResult<Void> setPrice(@RequestBody Map<String, Object> params) {
        Integer trainId = params.get("trainId") != null ?
                ((Number) params.get("trainId")).intValue() : null;
        Integer stationCount = params.get("stationCount") != null ?
                ((Number) params.get("stationCount")).intValue() : null;
        Double price = params.get("price") != null ?
                ((Number) params.get("price")).doubleValue() : null;

        if (trainId == null || stationCount == null || price == null) {
            return ApiResult.error(400, "参数不完整");
        }
        if (stationCount <= 0) {
            return ApiResult.error(400, "站点数必须大于0");
        }
        if (price < 0) {
            return ApiResult.error(400, "价格不能为负数");
        }

        // 验证车次是否存在
        ApiResult<Void> trainValidation = validateTrainExists(trainId);
        if (trainValidation != null) {
            return trainValidation;
        }

        boolean success = priceScheduleService.setPrice(trainId, stationCount, price);
        return success ? ApiResult.success("设置成功") : ApiResult.error(400, "设置失败");
    }

    @PostMapping("/batch")
    public ApiResult<Void> batchSetPrices(@RequestBody Map<String, Object> params) {
        Integer trainId = params.get("trainId") != null ?
                ((Number) params.get("trainId")).intValue() : null;
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> priceListRaw = (List<Map<String, Object>>) params.get("priceList");

        if (trainId == null || priceListRaw == null || priceListRaw.isEmpty()) {
            return ApiResult.error(400, "参数不完整");
        }

        // 验证车次是否存在
        ApiResult<Void> trainValidation = validateTrainExists(trainId);
        if (trainValidation != null) {
            return trainValidation;
        }

        // 验证是否已设置过价格梯度
        ApiResult<Void> priceValidation = validatePriceNotSet(trainId);
        if (priceValidation != null) {
            return priceValidation;
        }

        // 将 LinkedHashMap 转换为 PriceSchedule 实体
        List<PriceSchedule> priceList = new java.util.ArrayList<>();
        for (Map<String, Object> map : priceListRaw) {
            PriceSchedule ps = new PriceSchedule();
            Object stationCountObj = map.get("stationCount");
            Object priceObj = map.get("price");
            
            if (stationCountObj == null || priceObj == null) {
                return ApiResult.error(400, "价格列表参数不完整");
            }
            
            ps.setStationCount(((Number) stationCountObj).intValue());
            ps.setPrice(((Number) priceObj).doubleValue());
            priceList.add(ps);
        }

        boolean success = priceScheduleService.batchSetPrices(trainId, priceList);
        return success ? ApiResult.success("批量设置成功") : ApiResult.error(400, "批量设置失败");
    }

    @PostMapping("/check-complete")
    public ApiResult<Boolean> checkComplete(@RequestBody Map<String, Integer> params) {
        Integer trainId = params.get("trainId");
        Integer totalStationCount = params.get("totalStationCount");

        if (trainId == null || totalStationCount == null) {
            return ApiResult.error(400, "参数不完整");
        }

        boolean complete = priceScheduleService.isComplete(trainId, totalStationCount);
        return ApiResult.success("检查成功", complete);
    }
}
