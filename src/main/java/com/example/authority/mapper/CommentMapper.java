package com.example.authority.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.authority.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 评论列表 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-10-22
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}
