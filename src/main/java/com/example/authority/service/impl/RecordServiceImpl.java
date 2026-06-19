package com.example.authority.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.authority.entity.Record;
import com.example.authority.mapper.RecordMapper;
import com.example.authority.service.RecordService;
import org.springframework.stereotype.Service;


/**
 * @program: design
 * @ClassName:RecordServiceImpl
 * @description: service实现类
 * @author:dyy
 * @Version 3.0
 **/
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

}
