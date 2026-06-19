package com.example.authority.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.authority.entity.Article;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 健康资讯 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-10-22
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

}
