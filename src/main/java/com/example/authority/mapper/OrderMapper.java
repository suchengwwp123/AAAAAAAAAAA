package com.example.authority.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.authority.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 订单列表 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-10-23
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}
