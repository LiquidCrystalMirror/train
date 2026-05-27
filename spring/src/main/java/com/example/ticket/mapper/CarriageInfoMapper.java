package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.CarriageInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 车厢信息模板Mapper
 */
@Mapper
public interface CarriageInfoMapper extends BaseMapper<CarriageInfo> {
    
    /**
     * 查询所有车厢模板
     */
    List<CarriageInfo> selectAllTemplates();
}
