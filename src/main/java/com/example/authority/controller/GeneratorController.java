package com.example.authority.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.authority.annotations.ClearPerms;
import com.example.authority.common.Result;
import com.example.authority.entity.dto.*;
import com.example.authority.enums.ColumnTypeEnum;
import com.example.authority.service.DatabaseService;
import com.example.authority.service.GeneratorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.springframework.web.bind.annotation.*;

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
 * @program: authority-2026.0.2
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

    @PostMapping
    @ClearPerms
    public Result generator(@RequestBody GeneratorDto generatorDto) throws SQLException, ClassNotFoundException, InterruptedException, IOException {

        generatorService.generatorCode(generatorDto);
        return Result.success();
    }
    @PostMapping("/sqlgenerator")
    @ClearPerms
    public Result generatorBySql(@RequestBody GeneratorDto generatorDto) throws SQLException, ClassNotFoundException, InterruptedException, IOException {
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