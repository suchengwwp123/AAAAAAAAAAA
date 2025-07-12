package com.example.authority.service;

import com.example.authority.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;


/**
 * @program: authority-2026.0.3
 * @ClassName: UserService
 * @description: 用户表 service
 * @author:dyy
 * @Version 3.0
 **/

public interface UserService extends IService<User> {

    User getUserInfo();
}
