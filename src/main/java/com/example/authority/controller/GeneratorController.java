package com.example.authority.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.authority.annotations.ClearPerms;
import com.example.authority.annotations.EnvLimit;
import com.example.authority.common.Result;
import com.example.authority.entity.dto.*;
import com.example.authority.enums.ColumnTypeEnum;
import com.example.authority.service.AiService;
import com.example.authority.service.DatabaseService;
import com.example.authority.service.GeneratorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @program: authority-2026.0.3
 * @ClassName:GeneratorController
 * @description: 代码生成器前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@RestController
@RequestMapping("/generator")
@Slf4j
public class GeneratorController {
    @Resource
    private GeneratorService generatorService;
    @Resource
    private DatabaseService databaseService;
    @Resource
    private AiService aiService;
    @Resource
    private ChatClient deepseekClient;


    @EnvLimit
    @PostMapping
    @ClearPerms
    public Result generator(@RequestBody GeneratorDto generatorDto) throws SQLException, ClassNotFoundException, InterruptedException, IOException {

        generatorService.generatorCode(generatorDto);
        return Result.success();
    }
    @EnvLimit
    @PostMapping("/sqlgenerator")
    @ClearPerms
    public Result generatorBySql(@RequestBody GeneratorDto generatorDto) throws SQLException, ClassNotFoundException, InterruptedException, IOException {

        return Result.success(generatorService.generatorBySql(generatorDto));
    }

    @PostMapping("/ai")
    @ClearPerms
    public Result generatorByAI(@RequestBody GeneratorDto generatorDto) throws SQLException, ClassNotFoundException, InterruptedException, IOException {

        String sql = deepseekClient.prompt().user(
                "请生成一个" + generatorDto.getTableName() + "表的sql语句，表的注释为" + generatorDto.getDescription() + "," +
                        "请确保表字段中以  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '序列号'开头，" +
                        "属性类型和长度只使用varchar(64)、varchar(255)、int(1)、int(5)、int(10)、bigint(20)、double(10,2)、datetime、text、longtext这几种," +
                        "属性最后两个字段是create_time创建时间和update_time修改时间" +
                        "属性的注释只用写属性的名称，不要写介绍" +
                        "设置id为主键 PRIMARY KEY (`id`) USING BTREE，" +
                        "对表的配置为ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='" +
                        generatorDto.getDescription() + "'" +
                        "复杂度在毕设水平,字段的数量少一些，" +
                        "返回不要带其他的格式，只返回sql语句,请勿返回其他内容!"
        ).call().content();

//        generatorDto.setSql();

        String cleaned = StrUtil.replace(sql, "```sql", "");
        cleaned = StrUtil.replace(cleaned, "```", "");

        generatorDto.setSql(cleaned);

        return Result.success(generatorService.generatorBySql(generatorDto));
    }

    /**
     * 获取当前数据库中的所有表以及所有列
     *
     * @return
     */
    @GetMapping("/tables")
    public Result getAllTablesAndColumns() {
        List<TableInfo> tableInfos = databaseService.getAllTables();
        List<Options> options = new ArrayList<>();
        for (TableInfo tableInfo : tableInfos) {
            Options option = new Options().toBuilder().label(tableInfo.getTableName()).value(tableInfo.getTableName()).build();
            tableInfo.getColumns().stream().forEach(columnInfo -> {
                option.getChildren().add(new Options().toBuilder().value(columnInfo.getDataType() + "/" + StrUtil.toCamelCase(columnInfo.getColumnName())).label(StrUtil.toCamelCase(columnInfo.getColumnName())).build());
            });
            options.add(option);
        }
        return Result.success(options);
    }

}