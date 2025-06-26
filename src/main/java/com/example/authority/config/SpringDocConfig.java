package com.example.authority.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: authority-2026.0.2
 * @ClassName:SpringDocConfig
 * @description: 接口文档SwaagerUI的配置类
 * 主要是配置Swaager页面的一些页面介绍参数等等
 * @author:dyy
 * @Version 3.0
 **/
@Configuration
@Slf4j
public class SpringDocConfig {
    @Bean
    public OpenAPI restfulOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("后台Swagger的api文档")
                        .description("api文档，用于指定前后端访问api规范，方便测试者对接口进行可视化测试！")
                        .version("v2026")
                        .license(new License().name("").url("")));
    }

}
