package com.example.ticket.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.INPUT)
    private String userId;  // 用户ID：定长随机字符串（如U1234567890）
    private String username;  // 用户名（唯一）
    private String password;
    private String realName;
    private String idCard;
    private String phone;
    private String role;
    private LocalDateTime createTime;
}