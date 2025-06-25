package com.example.authority.mapper;

import com.example.authority.entity.dto.ColumnInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DatabaseMapper {
    // 获取当前数据库的所有表
    @Select("SELECT table_name FROM information_schema.tables WHERE table_schema = (SELECT DATABASE())")
    List<String> getAllTables();


    // 获取指定表的所有列信息
    @Select("SELECT column_name, data_type, column_comment " +
            "FROM information_schema.columns " +
            "WHERE table_schema = (SELECT DATABASE()) AND table_name = #{tableName}")
    List<ColumnInfo> getColumnsByTableName(@Param("tableName") String tableName);
}
