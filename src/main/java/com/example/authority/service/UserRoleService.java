package com.example.authority.service;

import com.example.authority.entity.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;


/**
 * @program: authority-2026.0.2
 * @ClassName: UserRoleService
 * @description: 用户角色表 service
 * @author:dyy
 * @Version 3.0
 **/

public interface UserRoleService extends IService<UserRole> {

    List<Long> getRoleIds(Object loginId, String loginType);
}
