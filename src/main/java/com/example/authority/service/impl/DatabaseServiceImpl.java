package com.example.authority.service.impl;

import com.example.authority.entity.dto.ColumnInfo;
import com.example.authority.entity.dto.TableInfo;
import com.example.authority.mapper.DatabaseMapper;
import com.example.authority.service.DatabaseService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseServiceImpl implements DatabaseService {
    @Resource
    private DatabaseMapper databaseMapper;
    @Override
    public List<TableInfo> getAllTables() {


        List<String> tableNames = databaseMapper.getAllTables();
        List<TableInfo> tableInfos = new ArrayList<>();

        for (String tableName : tableNames) {
            List<ColumnInfo> columns = databaseMapper.getColumnsByTableName(tableName);
            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableName(tableName);
            tableInfo.setColumns(columns);
            tableInfos.add(tableInfo);
        }


        return tableInfos;
    }
}
