package com.example.authority.entity.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @program: authority-2026.0.2
 * @ClassName:SqlDto
 * @description: SQL语句构建类
 * @author:dyy
 * @Version 3.0
 **/
@Data
@Accessors(chain = true)
public class SqlDto implements Serializable {
    private static final long serialVersionUID = 1L;
    //    名称
    private String name;
    //    字段类型和常用长度
    private String typeAndsize;
    //    是否必选
    private Boolean isRequire = false;
    //    注释描述
    private String description;
    //  默认值
    private String defaultValue;

    //    表单组件
    private String formComponent;
    //   关联表信息
    private String[] relevance;
    //   关联实体类名称
    private String entityClass;
    //   关联实体类对象
    private String objectName;
    //    关联属性
    private String columnName;
    //    关联属性类名
    private String columnType;
    //    是否展示组件
    private Boolean isShow = false;
    //    是否展示查询
    private Boolean isSearch = false;
    //    查询数据类型
    private String searchType;
    //    查询数据名称
    private String searchName;
    //    get方法名
    private String gMethod;
    //    set方法名
    private String sMethod;
    //    getlamda名
    private String gLamda;
    //    setlamda方法名
    private String sLamda;
   //    字典值
    private Long dictId;
   //  关联字段的set方法值
    private String setRMethod;
//    关联字段的get方法值
    private String getRMethod;
    private String key;
    private String key1;
    private String key2;
    private String key3;
    private String key4;
    private String key5;
    private String key6;
    private String key7;
    private String key8;
    private String key9;
}