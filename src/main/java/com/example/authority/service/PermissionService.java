package com.example.authority.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.authority.entity.Permission;

import java.util.List;


/**
 * @program: authority-2026.0.1
 * @ClassName: PermissionService
 * @description: 菜单表 service
 * @author:dyy
 * @Version 3.0
 **/

public interface PermissionService extends IService<Permission> {
    List<Permission> buildTrees(List<Permission> permissions);

    List<Permission> getRouters(Object loginId, String loginType);
}
