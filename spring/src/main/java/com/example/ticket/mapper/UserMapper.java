package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.ticket.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 按角色统计用户数量
     */
    @Select("SELECT COUNT(*) FROM user WHERE role = #{role}")
    Long countByRole(@Param("role") String role);
    
    /**
     * 分页查询用户，支持按真实姓名模糊搜索
     */
    @Select("<script>" +
            "SELECT * FROM user " +
            "<where>" +
            "<if test='find != null and find != \"\"'>" +
            "real_name LIKE CONCAT('%', #{find}, '%')" +
            "</if>" +
            "</where>" +
            "</script>")
    Page<User> selectUserPage(Page<User> page, @Param("find") String find);
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);
    
    /**
     * 根据用户ID查询用户
     */
    @Select("SELECT * FROM user WHERE user_id = #{userId}")
    User selectByUserId(@Param("userId") String userId);
}