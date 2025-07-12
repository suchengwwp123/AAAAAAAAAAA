package com.example.authority.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.authority.entity.UserRole;
import com.example.authority.mapper.UserRoleMapper;
import com.example.authority.service.UserRoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @program: authority-2026.0.3
 * @ClassName:UserRoleServiceImpl
 * @description: 用户角色表 service实现类
 * @author:dyy
 * @Version 3.0
 **/
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {
    /**
     * 获取roleIds
     *
     * @param loginId
     * @param loginType
     * @return
     */
    @Override
    public List<Long> getRoleIds(Object loginId, String loginType) {

        //        基于id获取userolelist
        LambdaQueryWrapper<UserRole> userRoleLambdaQueryWrapper = new LambdaQueryWrapper<UserRole>().in(UserRole::getUserId, loginId);
//        基于userolelist转换成roleIds
        return this.list(userRoleLambdaQueryWrapper).stream().map(UserRole::getRoleId).collect(Collectors.toList());
    }

}
