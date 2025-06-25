package com.example.authority.service;

import com.example.authority.entity.dto.TableInfo;

import java.util.List;

public interface DatabaseService {
    public List<TableInfo> getAllTables();

}
