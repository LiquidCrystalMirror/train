package com.example.ticket.exception;

import com.example.ticket.util.ApiResult;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理主键冲突、唯一约束冲突（id重复、name重复）
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResult<?> handleDuplicateKeyException(DuplicateKeyException e) {
        // 判断是哪个字段冲突
        String message = e.getMessage();
        if (message.contains("PRIMARY")) {
            return ApiResult.error(400, "用户ID已存在");
        } else if (message.contains("name")) {
            return ApiResult.error(400, "用户名已存在");
        }
        return ApiResult.error(400, "数据已存在");
    }

    /**
     * 处理数据库完整性约束异常（NOT NULL 等）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResult<?> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String message = e.getMessage();
        if (message.contains("NOT NULL")) {
            return ApiResult.error(400, "缺少必要参数");
        }
        return ApiResult.error(400, "数据完整性错误");
    }

    /**
     * 处理 JSON 解析错误（缺少逗号等格式错误）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResult<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ApiResult.error(400, "请求参数格式错误，请检查JSON格式");
    }

    /**
     * 处理参数类型不匹配（如 String 传给 Integer）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResult<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ApiResult.error(400, "参数类型错误：" + e.getName() + " 类型不正确");
    }

    /**
     * 处理缺少参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResult<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return ApiResult.error(400, "缺少必要参数：" + e.getParameterName());
    }

    /**
     * 处理参数验证失败（配合 @Valid 使用）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResult.error(400, message);
    }

    /**
     * 处理自定义业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResult<?> handleBusinessException(BusinessException e) {
        return ApiResult.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理日期时间解析异常
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ApiResult<?> handleDateTimeParseException(DateTimeParseException e) {
        return ApiResult.error(400, "日期时间格式错误：" + e.getParsedString() + "，请使用 ISO-8601 格式（如 2025-06-01T10:00:00）");
    }

    /**
     * 处理认证授权异常（Token 无效、未登录）
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResult<?> handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();
        
        // 判断是否是认证相关错误
        if (message.contains("未登录") || message.contains("登录已过期")) {
            return ApiResult.error(401, message);
        }
        
        // 判断是否是权限相关错误
        if (message.contains("权限不足")) {
            return ApiResult.error(403, message);
        }
        
        // 判断是否是 Token 相关错误
        if (message.contains("认证") || message.contains("Token")) {
            return ApiResult.error(401, message);
        }
        
        // 其他运行时异常按 500 处理
        e.printStackTrace();
        return ApiResult.error(500, "服务器内部错误：" + message);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResult<?> handleException(Exception e) {
        e.printStackTrace();
        return ApiResult.error(500, "服务器内部错误：" + e.getMessage());
    }
}