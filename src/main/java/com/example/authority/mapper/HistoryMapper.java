package com.example.authority.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.authority.entity.History;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 聊天记录 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-06-28
 */
@Mapper
public interface HistoryMapper extends BaseMapper<History> {

}
