package com.example.authority.service.impl;

import com.example.authority.entity.Student;
import com.example.authority.mapper.StudentMapper;
import com.example.authority.service.StudentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


/**
 * @program: authority-2026.0.2
 * @ClassName:StudentServiceImpl
 * @description: 学生列表 service实现类
 * @author:dyy
 * @Version 3.0
 **/
@Service
        public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>implements StudentService {

        }
