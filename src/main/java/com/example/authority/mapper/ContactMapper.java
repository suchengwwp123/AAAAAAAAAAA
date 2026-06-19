package com.example.authority.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.authority.entity.Contact;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 联系记录 Mapper 接口
 * </p>
 *
 * @author baomidou
 * @since 2025-10-21
 */
@Mapper
public interface ContactMapper extends BaseMapper<Contact> {

}
