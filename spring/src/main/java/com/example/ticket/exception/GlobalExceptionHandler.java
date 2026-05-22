package com.example.ticket.exception;

import com.example.ticket.util.RespEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理主键冲突、唯一约束冲突（id重复、name重复）
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public RespEntity handleDuplicateKeyException(DuplicateKeyException e) {
        // 判断是哪个字段冲突
        String message = e.getMessage();
        if (message.contains("PRIMARY")) {
            return new RespEntity(400, "用户ID已存在", null);
        } else if (message.contains("name")) {
            return new RespEntity(400, "用户名已存在", null);
        }
        return new RespEntity(400, "数据已存在", null);
    }

    /**
     * 处理数据库完整性约束异常（NOT NULL 等）
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public RespEntity handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String message = e.getMessage();
        if (message.contains("NOT NULL")) {
            return new RespEntity(400, "缺少必要参数", null);
        }
        return new RespEntity(400, "数据完整性错误", null);
    }

    /**
     * 处理 JSON 解析错误（缺少逗号等格式错误）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public RespEntity handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return new RespEntity(400, "请求参数格式错误，请检查JSON格式", null);
    }

    /**
     * 处理参数类型不匹配（如 String 传给 Integer）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public RespEntity handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return new RespEntity(400, "参数类型错误：" + e.getName() + " 类型不正确", null);
    }

    /**
     * 处理缺少参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public RespEntity handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return new RespEntity(400, "缺少必要参数：" + e.getParameterName(), null);
    }

    /**
     * 处理参数验证失败（配合 @Valid 使用）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RespEntity handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return new RespEntity(400, message, null);
    }

    /**
     * 处理认证授权异常（Token 无效、未登录）
     */
    @ExceptionHandler(RuntimeException.class)
    public RespEntity handleRuntimeException(RuntimeException e) {
        String message = e.getMessage();
        
        // 判断是否是认证相关错误
        if (message.contains("未登录") || message.contains("登录已过期")) {
            return new RespEntity(401, message, null);
        }
        
        // 判断是否是权限相关错误
        if (message.contains("权限不足")) {
            return new RespEntity(403, message, null);
        }
        
        // 判断是否是 Token 相关错误
        if (message.contains("认证") || message.contains("Token")) {
            return new RespEntity(401, message, null);
        }
        
        // 其他运行时异常按 500 处理
        e.printStackTrace();
        return new RespEntity(500, "服务器内部错误：" + message, null);
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public RespEntity handleException(Exception e) {
        e.printStackTrace();
        return new RespEntity(500, "服务器内部错误：" + e.getMessage(), null);
    }
}