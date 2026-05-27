package com.example.ticket.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.RefundInfo;
import com.example.ticket.entity.SaleInfo;
import com.example.ticket.entity.TrainInfo;
import com.example.ticket.entity.TicketInfo;
import com.example.ticket.entity.User;
import com.example.ticket.mapper.RefundInfoMapper;
import com.example.ticket.mapper.SaleInfoMapper;
import com.example.ticket.mapper.TrainInfoMapper;
import com.example.ticket.mapper.TicketInfoMapper;
import com.example.ticket.mapper.UserMapper;
import com.example.ticket.util.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计和订单管理Controller
 */
@RestController
@RequestMapping("/api/v1")
public class StatsController {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TrainInfoMapper trainInfoMapper;

    @Resource
    private TicketInfoMapper ticketInfoMapper;

    @Resource
    private SaleInfoMapper saleInfoMapper;

    @Resource
    private RefundInfoMapper refundInfoMapper;

    // ==================== 统计接口 ====================

    /**
     * 获取系统统计数据
     */
    @GetMapping("/stats/system")
    public ApiResult<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 用户总数
        long userCount = userMapper.selectCount(null);
        stats.put("userCount", userCount);
        
        // 车次总数
        long trainCount = trainInfoMapper.selectCount(null);
        stats.put("trainCount", trainCount);
        
        // 车票总数
        long ticketCount = ticketInfoMapper.selectCount(null);
        stats.put("ticketCount", ticketCount);
        
        // 订单总数
        long orderCount = saleInfoMapper.selectCount(null);
        stats.put("orderCount", orderCount);
        
        return ApiResult.success("查询成功", stats);
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/stats/users")
    public ApiResult<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总用户数
        long totalCount = userMapper.selectCount(null);
        stats.put("totalCount", totalCount);
        
        // 管理员数量
        long adminCount = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getRole, "admin"));
        stats.put("adminCount", adminCount);
        
        // 普通用户数量
        long userCount = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                .eq(User::getRole, "user"));
        stats.put("userCount", userCount);
        
        return ApiResult.success("查询成功", stats);
    }

    /**
     * 获取车次统计
     */
    @GetMapping("/stats/trains")
    public ApiResult<Map<String, Object>> getTrainStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总车次数
        long totalCount = trainInfoMapper.selectCount(null);
        stats.put("totalCount", totalCount);
        
        // 有路线的车次数
        long withRouteCount = trainInfoMapper.selectCount(Wrappers.<TrainInfo>lambdaQuery()
                .isNotNull(TrainInfo::getRouterId));
        stats.put("withRouteCount", withRouteCount);
        
        return ApiResult.success("查询成功", stats);
    }

    /**
     * 获取车票统计
     */
    @GetMapping("/stats/tickets")
    public ApiResult<Map<String, Object>> getTicketStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总票数
        long totalCount = ticketInfoMapper.selectCount(null);
        stats.put("totalCount", totalCount);
        
        // 可售票数
        long availableCount = ticketInfoMapper.selectCount(Wrappers.<TicketInfo>lambdaQuery()
                .eq(TicketInfo::getTicketStatus, "available"));
        stats.put("availableCount", availableCount);
        
        // 已售票数
        long soldCount = ticketInfoMapper.selectCount(Wrappers.<TicketInfo>lambdaQuery()
                .eq(TicketInfo::getTicketStatus, "sold"));
        stats.put("soldCount", soldCount);
        
        return ApiResult.success("查询成功", stats);
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/stats/orders")
    public ApiResult<Map<String, Object>> getOrderStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 总订单数
        long totalCount = saleInfoMapper.selectCount(null);
        stats.put("totalCount", totalCount);
        
        // 已出票订单数
        long issuedCount = saleInfoMapper.selectCount(Wrappers.<SaleInfo>lambdaQuery()
                .eq(SaleInfo::getSaleStatus, "已出票"));
        stats.put("issuedCount", issuedCount);
        
        // 已退票订单数
        long refundedCount = saleInfoMapper.selectCount(Wrappers.<SaleInfo>lambdaQuery()
                .eq(SaleInfo::getSaleStatus, "已退票"));
        stats.put("refundedCount", refundedCount);
        
        return ApiResult.success("查询成功", stats);
    }

    // ==================== 用户订单接口 ====================

    /**
     * 查询用户的购票记录
     */
    @GetMapping("/sale/user/{userId}")
    public ApiResult<List<SaleInfo>> getUserPurchases(@PathVariable String userId) {
        List<SaleInfo> sales = saleInfoMapper.selectList(Wrappers.<SaleInfo>lambdaQuery()
                .eq(SaleInfo::getUserId, userId)
                .orderByDesc(SaleInfo::getSaleTime));
        return ApiResult.success("查询成功", sales);
    }

    /**
     * 查询用户的退票记录
     */
    @GetMapping("/refund/user/{userId}")
    public ApiResult<List<RefundInfo>> getUserRefunds(@PathVariable String userId) {
        List<RefundInfo> refunds = refundInfoMapper.selectList(Wrappers.<RefundInfo>lambdaQuery()
                .eq(RefundInfo::getUserId, userId)
                .orderByDesc(RefundInfo::getRefundTime));
        return ApiResult.success("查询成功", refunds);
    }

    /**
     * 分页查询用户的购票记录
     */
    @PostMapping("/sale/user/page")
    public ApiResult<Page<SaleInfo>> getUserPurchasePage(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        int pageSize = params.containsKey("pageSize") ? (Integer) params.get("pageSize") : 10;
        String userId = (String) params.get("userId");
        
        Page<SaleInfo> page = new Page<>(pageNum, pageSize);
        saleInfoMapper.selectPage(page, Wrappers.<SaleInfo>lambdaQuery()
                .eq(SaleInfo::getUserId, userId)
                .orderByDesc(SaleInfo::getSaleTime));
        
        return ApiResult.success("查询成功", page);
    }

    /**
     * 分页查询用户的退票记录
     */
    @PostMapping("/refund/user/page")
    public ApiResult<Page<RefundInfo>> getUserRefundPage(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        int pageSize = params.containsKey("pageSize") ? (Integer) params.get("pageSize") : 10;
        String userId = (String) params.get("userId");
        
        Page<RefundInfo> page = new Page<>(pageNum, pageSize);
        refundInfoMapper.selectPage(page, Wrappers.<RefundInfo>lambdaQuery()
                .eq(RefundInfo::getUserId, userId)
                .orderByDesc(RefundInfo::getRefundTime));
        
        return ApiResult.success("查询成功", page);
    }

    // ==================== 售票列表接口 ====================

    /**
     * 查询所有售票记录（管理员）
     */
    @PostMapping("/sale/list")
    public ApiResult<Page<SaleInfo>> getAllSales(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        int pageSize = params.containsKey("pageSize") ? (Integer) params.get("pageSize") : 10;
        
        Page<SaleInfo> page = new Page<>(pageNum, pageSize);
        saleInfoMapper.selectPage(page, Wrappers.<SaleInfo>lambdaQuery()
                .orderByDesc(SaleInfo::getSaleTime));
        
        return ApiResult.success("查询成功", page);
    }

    /**
     * 根据车次ID查询售票记录
     */
    @GetMapping("/sale/train/{trainId}")
    public ApiResult<List<SaleInfo>> getSalesByTrain(@PathVariable Integer trainId) {
        List<SaleInfo> sales = saleInfoMapper.selectList(Wrappers.<SaleInfo>lambdaQuery()
                .eq(SaleInfo::getTrainId, trainId)
                .orderByDesc(SaleInfo::getSaleTime));
        return ApiResult.success("查询成功", sales);
    }

    /**
     * 根据车票ID查询售票记录
     */
    @GetMapping("/sale/ticket/{ticketId}")
    public ApiResult<List<SaleInfo>> getSalesByTicket(@PathVariable Integer ticketId) {
        List<SaleInfo> sales = saleInfoMapper.selectList(Wrappers.<SaleInfo>lambdaQuery()
                .eq(SaleInfo::getTicketId, ticketId)
                .orderByDesc(SaleInfo::getSaleTime));
        return ApiResult.success("查询成功", sales);
    }

    // ==================== 退票列表接口 ====================

    /**
     * 查询所有退票记录（管理员）
     */
    @PostMapping("/refund/list")
    public ApiResult<Page<RefundInfo>> getAllRefunds(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        int pageSize = params.containsKey("pageSize") ? (Integer) params.get("pageSize") : 10;
        
        Page<RefundInfo> page = new Page<>(pageNum, pageSize);
        refundInfoMapper.selectPage(page, Wrappers.<RefundInfo>lambdaQuery()
                .orderByDesc(RefundInfo::getRefundTime));
        
        return ApiResult.success("查询成功", page);
    }

    /**
     * 根据售票记录ID查询退票记录
     */
    @GetMapping("/refund/sale/{saleId}")
    public ApiResult<RefundInfo> getRefundBySale(@PathVariable Integer saleId) {
        RefundInfo refund = refundInfoMapper.selectOne(Wrappers.<RefundInfo>lambdaQuery()
                .eq(RefundInfo::getSaleId, saleId));
        return ApiResult.success("查询成功", refund);
    }
}
