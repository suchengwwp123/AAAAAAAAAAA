package com.example.authority.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.authority.entity.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 学生列表 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-06-26
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {

}
