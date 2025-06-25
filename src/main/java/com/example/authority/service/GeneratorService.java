package com.example.authority.service;

import com.example.authority.entity.dto.GeneratorDto;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @program: authority-2026.0.2
 * @ClassName: GeneratorService
 * @description: 代码生成器 GeneratorService
 * @author:dyy
 * @Version 3.0
 **/

public interface GeneratorService {
    void generatorCode(GeneratorDto generatorDto) throws SQLException, ClassNotFoundException, InterruptedException, IOException;
    GeneratorDto generatorBySql(GeneratorDto generatorDto);
}
