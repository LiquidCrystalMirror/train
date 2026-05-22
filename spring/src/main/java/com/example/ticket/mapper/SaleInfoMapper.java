package com.example.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.entity.SaleInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SaleInfoMapper extends BaseMapper<SaleInfo> {
    
    /**
     * 根据销售ID查询销售记录
     */
    @Select("SELECT * FROM sale_info WHERE sale_id = #{saleId}")
    SaleInfo selectBySaleId(@Param("saleId") Integer saleId);
    
    /**
     * 更新销售记录状态
     */
    @Update("UPDATE sale_info SET sale_status = #{status} WHERE sale_id = #{saleId}")
    int updateSaleStatus(@Param("saleId") Integer saleId, @Param("status") String status);
}