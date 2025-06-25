package com.example.authority.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.authority.entity.Files;
import com.example.authority.mapper.FileMapper;
import com.example.authority.service.FileService;
import org.springframework.stereotype.Service;

/**
 * @program: authority
 * @ClassName 文件上传 service实现类
 * @description:
 * @Version 3.0
 **/
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, Files> implements FileService {

}
