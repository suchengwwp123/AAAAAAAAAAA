package com.example.authority;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.TokenSign;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.example.authority.service.UserRoleService;
import com.example.authority.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

@SpringBootTest
@Slf4j
class AuthorityApplicationTests {
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value(("${spring.datasource.password}"))
    private String password;

    @Resource
    private UserRoleService userRoleService;
    @Resource
    private UserService userService;



    @Test
    void contextLoads() {
        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder
                            .disableOpenDir()
                            .fileOverride()// 覆盖已生成文件
                            .enableSwagger()//启动swagger模式
                            .outputDir(System.getProperty("user.dir") + "/src/main/java/"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.example.authority") // 设置父包名
                            .moduleName(null) // 设置父包模块名
                            .entity("entity")
                            .service("service")
                            .serviceImpl("service.impl")
                            .mapper("mapper")
                            .xml("mapper.xml")
                            .controller("controller")
                            .pathInfo(Collections.singletonMap(OutputFile.xml, System.getProperty("user.dir") + "/src/main/resources/mapper/")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("sys_student") // 设置需要生成的表名
                            .addTablePrefix("sys")
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")
                            .entityBuilder()
                            .enableLombok()
                            .logicDeleteColumnName("deleted")
                            .enableTableFieldAnnotation()
                            .controllerBuilder()
                            .enableRestStyle()
                            .formatFileName("%sController")
                            .mapperBuilder()
                            .enableBaseResultMap()  //生成通用的resultMap
                            .superClass(BaseMapper.class)
                            .formatMapperFileName("%sMapper")
                            .enableMapperAnnotation()
                            .formatXmlFileName("%sMapper");
                    // 设置过滤表前缀 可以在Constant自行设置
                })
                // 使用Velocity引擎模板
                .execute();
    }

    @Test
    void testOnLine() {
// 获取所有已登录的会话id
        List<String> sessionIdList = StpUtil.searchSessionId("", 0, -1, false);

        for (String sessionId : sessionIdList) {

            // 根据会话id，查询对应的 SaSession 对象，此处一个 SaSession 对象即代表一个登录的账号
            SaSession session = StpUtil.getSessionBySessionId(sessionId);

            // 查询这个账号都在哪些设备登录了，依据上面的示例，账号A 的 SaTerminalInfo 数量是 3，账号B 的 SaTerminalInfo 数量是 2
            List<TokenSign> terminalList = session.tokenSignListCopy() ;
//            System.out.println("会话id：" + sessionId + "，共在 " + terminalList.size() + " 设备登录");
        }

    }




}
