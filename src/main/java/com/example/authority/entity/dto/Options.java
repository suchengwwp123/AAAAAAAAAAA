package com.example.authority.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 获取数据表以及属性需要用的dto数据结构
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Options {
    private String label;
    private String value;
    private List<Options> children=new ArrayList<>();
}
