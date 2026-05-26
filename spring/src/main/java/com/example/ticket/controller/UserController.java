package com.example.ticket.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.User;
import com.example.ticket.mapper.UserMapper;
import com.example.ticket.service.UserService;
import com.example.ticket.util.JwtUtil;
import com.example.ticket.util.PasswordUtil;
import com.example.ticket.util.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    @Resource
    private JwtUtil jwtUtil;

    // ===================== 统计用户数量 =====================
    @GetMapping("/g/allTotal")
    public ApiResult<Map<String, Object>> getAllTotal() {
        Map<String, Object> map = new HashMap<>();
        map.put("admin", userMapper.countByRole("admin"));
        map.put("user", userMapper.countByRole("user"));
        return ApiResult.success("查询成功", map);
    }

    // ===================== 分页查询用户 =====================
    @PostMapping("/g/user")
    public ApiResult<Page<User>> userPage(@RequestBody Map<String, Object> params) {
        int pageNum = params.containsKey("pageNum") ? (Integer) params.get("pageNum") : 1;
        String find = params.containsKey("find") ? (String) params.get("find") : "";

        Page<User> page = new Page<>(pageNum, 6);
        userMapper.selectUserPage(page, find);

        return ApiResult.success("查询成功", page);
    }

    // ===================== 查询所有用户 =====================
    @GetMapping("/g/users")
    public ApiResult<List<User>> userList() {
        List<User> list = userService.list();
        return ApiResult.success("成功", list);
    }

    // ===================== 登录 =====================
    @PostMapping("/login")
    public ApiResult<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        String account = params.get("account");  // 支持 userId 或 username
        String password = params.get("password");

        if (account == null || account.trim().isEmpty()) {
            return ApiResult.error(400, "缺少必要参数：account");
        }
        if (password == null || password.trim().isEmpty()) {
            return ApiResult.error(400, "缺少必要参数：password");
        }

        // 先尝试按 userId 查询，再按 username 查询
        User user = userMapper.selectByUserId(account);
        if (user == null) {
            user = userMapper.selectByUsername(account);
        }

        if (user == null) {
            return ApiResult.error(400, "用户不存在");
        }

        // 密码校验
        if (!passwordUtil.verifyPassword(password, user.getPassword())) {
            return ApiResult.error(400, "密码错误");
        }

        // 生成 token（使用JwtUtil）
        String token = jwtUtil.generateToken(user);
        
        // 安全处理：移除密码字段后再返回
        User safeUser = new User();
        safeUser.setUserId(user.getUserId());
        safeUser.setUsername(user.getUsername());
        safeUser.setRealName(user.getRealName());
        safeUser.setIdCard(user.getIdCard());
        safeUser.setPhone(user.getPhone());
        safeUser.setRole(user.getRole());
        safeUser.setCreateTime(user.getCreateTime());
        
        Map<String, Object> map = new HashMap<>();
        map.put("user", safeUser);
        map.put("token", token);

        return ApiResult.success("登录成功", map);
    }

    // ===================== 注册 =====================
    @PostMapping("/reg")
    public ApiResult<User> register(@RequestBody User user) {
        // 检查用户名是否存在
        User exist = userMapper.selectByUsername(user.getUsername());

        if (exist != null) {
            return ApiResult.error(400, "用户名已存在");
        }

        // 生成随机用户ID
        String userId = passwordUtil.generateUserId();
        user.setUserId(userId);
        
        // 安全处理：强制设置角色为普通用户，防止越权注册
        user.setRole("user");

        // 对密码进行加密处理
        String encryptedPassword = passwordUtil.md5WithSalt(user.getPassword());
        user.setPassword(encryptedPassword);
        
        user.setCreateTime(LocalDateTime.now());
        userService.save(user);
        
        // 返回时包含userId，方便用户记住
        user.setPassword(null);

        return ApiResult.success("注册成功，请牢记您的用户ID：" + userId, user);
    }

    // ===================== 管理员注册 =====================
    @PostMapping("/admin/reg")
    public ApiResult<User> adminRegister(@RequestBody User user) {
        // 检查用户名是否存在
        User exist = userMapper.selectByUsername(user.getUsername());

        if (exist != null) {
            return ApiResult.error(400, "用户名已存在");
        }

        // 生成随机用户ID
        String userId = passwordUtil.generateUserId();
        user.setUserId(userId);
        
        // 强制设置为管理员
        user.setRole("admin");

        // 对密码进行加密处理
        String encryptedPassword = passwordUtil.md5WithSalt(user.getPassword());
        user.setPassword(encryptedPassword);
        
        user.setCreateTime(LocalDateTime.now());
        userService.save(user);
        
        // 返回时包含userId
        user.setPassword(null);

        return ApiResult.success("管理员注册成功，请牢记您的用户ID：" + userId, user);
    }

    // ===================== 用户修改自己信息 =====================
    @PutMapping("/user/edit")
    public ApiResult<Void> edit(@RequestBody User user, HttpServletRequest request) {
        User loginUser = (User) request.getAttribute("auth");

        User update = new User();
        update.setUserId(loginUser.getUserId());
        update.setRealName(user.getRealName());
        update.setIdCard(user.getIdCard());
        update.setPhone(user.getPhone());

        userService.updateById(update);
        return ApiResult.success("修改成功");
    }

    // ===================== 管理员修改用户信息 =====================
    @PutMapping("/admin/user/update")
    public ApiResult<Void> adminUpdate(@RequestBody User user, HttpServletRequest request) {
        User login = (User) request.getAttribute("auth");
        if (!"admin".equals(login.getRole())) {
            return ApiResult.error(403, "无权限");
        }

        userService.updateById(user);
        return ApiResult.success("修改成功");
    }

    // ===================== 管理员删除用户 =====================
    @DeleteMapping("/admin/user/{id}")
    public ApiResult<Void> delete(@PathVariable Integer id, HttpServletRequest request) {
        User login = (User) request.getAttribute("auth");
        if (!"admin".equals(login.getRole())) {
            return ApiResult.error(403, "无权限");
        }

        userService.removeById(id);
        return ApiResult.success("删除成功");
    }
}