package com.example.ticket.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.User;
import com.example.ticket.mapper.UserMapper;
import com.example.ticket.service.UserService;
import com.example.ticket.util.PasswordUtil;
import com.example.ticket.util.RespEntity;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户控制器（完全适配火车售票系统 User 实体）
 */
@RestController
@RequestMapping("/api/v1")
public class UserController {

    @Resource
    private UserService userService;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private PasswordUtil passwordUtil;

    @Value("${my.jwt_pwd}")
    private String jwtPwd;

    // ===================== 统计用户数量 =====================
    @GetMapping("/g/allTotal")
    public RespEntity getAllTotal() {
        Map<String, Object> map = new HashMap<>();
        map.put("admin", userMapper.countByRole("admin"));
        map.put("user", userMapper.countByRole("user"));
        return new RespEntity(2000, "查询成功", map);
    }

    // ===================== 分页查询用户 =====================
    @PostMapping("/g/user")
    public RespEntity userPage(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        String find = params.containsKey("find") ? (String) params.get("find") : "";

        Page<User> page = new Page<>(pageNum, 6);
        userMapper.selectUserPage(page, find);

        return new RespEntity(2000, "查询成功", page);
    }

    // ===================== 查询所有用户 =====================
    @GetMapping("/g/users")
    public RespEntity userList() {
        List<User> list = userService.list();
        return new RespEntity(2000, "成功", list);
    }

    // ===================== 登录 =====================
    @PostMapping("/login")
    public RespEntity login(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role) {

        User user = userMapper.selectByUsername(username);

        if (user == null) {
            return new RespEntity(4001, "用户名或密码错误", null);
        }

        // 角色校验
        if (!user.getRole().equals(role)) {
            return new RespEntity(4002, "角色不匹配", null);
        }

        // 密码校验
        if (!passwordUtil.verifyPassword(password, user.getPassword())) {
            return new RespEntity(4004, "密码错误", null);
        }

        // 生成 token
        String token = generateJwtToken(user);
        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("token", token);

        return new RespEntity(2000, "登录成功", map);
    }

    // ===================== 注册 =====================
    @PostMapping("/reg")
    public RespEntity register(@RequestBody User user) {
        // 检查用户名是否存在
        User exist = userMapper.selectByUsername(user.getUsername());

        if (exist != null) {
            return new RespEntity(4000, "用户名已存在", null);
        }

        // 默认普通用户
        if (user.getRole() == null) {
            user.setRole("user");
        }

        // 对密码进行加密处理
        String encryptedPassword = passwordUtil.md5WithSalt(user.getPassword());
        user.setPassword(encryptedPassword);
        
        user.setCreateTime(LocalDateTime.now());
        userService.save(user);
        user.setPassword(null);

        return new RespEntity(2000, "注册成功", user);
    }

    // ===================== JWT 生成（完全适配你的实体） =====================
    private String generateJwtToken(User user) {
        JwtBuilder builder = Jwts.builder();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        claims.put("realName", user.getRealName());
        claims.put("phone", user.getPhone());
        claims.put("role", user.getRole());

        return builder
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, jwtPwd)
                .compact();
    }

    // ===================== 用户修改自己信息 =====================
    @PutMapping("/user/edit")
    public RespEntity edit(@RequestBody User user, HttpServletRequest request) {
        User loginUser = (User) request.getAttribute("auth");

        User update = new User();
        update.setUserId(loginUser.getUserId());
        update.setRealName(user.getRealName());
        update.setIdCard(user.getIdCard());
        update.setPhone(user.getPhone());

        userService.updateById(update);
        return new RespEntity(2000, "修改成功", null);
    }

    // ===================== 管理员修改用户信息 =====================
    @PutMapping("/admin/user/update")
    public RespEntity adminUpdate(@RequestBody User user, HttpServletRequest request) {
        User login = (User) request.getAttribute("auth");
        if (!"admin".equals(login.getRole())) {
            return new RespEntity(403, "无权限", null);
        }

        userService.updateById(user);
        return new RespEntity(2000, "修改成功", null);
    }

    // ===================== 管理员删除用户 =====================
    @DeleteMapping("/admin/user/{id}")
    public RespEntity delete(@PathVariable Integer id, HttpServletRequest request) {
        User login = (User) request.getAttribute("auth");
        if (!"admin".equals(login.getRole())) {
            return new RespEntity(403, "无权限", null);
        }

        userService.removeById(id);
        return new RespEntity(2000, "删除成功", null);
    }
}