package com.example.authority.entity.dto;

import com.example.authority.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: authority
 * @ClassName:Reply
 * @description: 回复内容
 * @author:kkkklee
 * @create:
 * @Version 3.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Reply {
    private int total=0;
    private List<Comment> list=new ArrayList<>();
}