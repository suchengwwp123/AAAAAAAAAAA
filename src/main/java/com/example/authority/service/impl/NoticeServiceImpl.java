package com.example.authority.service.impl;

import com.example.authority.entity.Notice;
import com.example.authority.mapper.NoticeMapper;
import com.example.authority.service.NoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


/**
 * @program: design
 * @ClassName:NoticeServiceImpl
 * @description: 系统公告 service实现类
 * @author:dyy
 * @Version 3.0
 **/
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

}
