package com.example.authority.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Cloud环境下的Sa-Token配置（不使用Redis，改用内存存储）
 */
@Configuration
@Profile("cloud")
public class SaTokenCloudConfig {

    @Bean
    public SaTokenDao saTokenDao() {
        // 使用Sa-Token默认的内存存储实现
        return new SaTokenDaoDefaultImpl();
    }
}
