package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.Router;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RouterMapper extends BaseMapper<Router> {
    // 可扩展自定义查询方法
}