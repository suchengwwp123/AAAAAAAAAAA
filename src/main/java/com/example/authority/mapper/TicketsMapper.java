package com.example.authority.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.authority.entity.Tickets;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 工单记录 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-10-23
 */
@Mapper
public interface TicketsMapper extends BaseMapper<Tickets> {

}
