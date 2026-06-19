package com.example.authority.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.db.Db;
import cn.hutool.db.ds.simple.SimpleDataSource;
import cn.hutool.json.JSONUtil;
import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineConfig;
import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.engine.EngineTemplateType;
import cn.smallbun.screw.core.execute.DocumentationExecute;
import cn.smallbun.screw.core.process.ProcessConfig;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.fill.Column;
import com.example.authority.common.Constant;
import com.example.authority.common.Result;
import com.example.authority.entity.Dict;
import com.example.authority.entity.Permission;
import com.example.authority.entity.Record;
import com.example.authority.entity.RolePermission;
import com.example.authority.entity.dto.GeneratorDto;
import com.example.authority.entity.dto.SqlDto;
import com.example.authority.enums.ColumnTypeEnum;
import com.example.authority.service.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @program: authority
 * @ClassName:GeneratorServiceImpl
 * @description: 代码生成器实现类
 * @author:dyy
 * @Version 3.0
 **/
@Service
@Slf4j

public class GeneratorServiceImpl implements GeneratorService {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    private String datasource = "com.mysql.cj.jdbc.Driver";
    private String path = System.getProperty("user.dir") + "/src/main/resources/static/doc/";
    @Resource
    private PermissionService permissionService;
    @Resource
    private RecordService recordService;
    @Resource
    private RolePermissionService rolePermissionService;
    @Resource
    private DictService dictService;

    /**
     * 构建代码方法
     * 1.判断表名字是否符合规范
     * 2.判断表是否为系统表，如果是系统表则无法覆盖掉
     * 3.先校验上传的数据是否存在逻辑错误，如果存在逻辑错误，则返回错误信息
     * 4.生成数据库表
     * 5.克隆数据，保留原始的数据
     * 6.对克隆的数据进行驼峰化
     * 7.生成后端代码
     * 8.生成前端代码
     * 9.生成权限数据
     * 10.生成文档
     * 11.保存记录
     *
     * @param generatorDto
     * @throws SQLException
     */
    @Override
    public void generatorCode(GeneratorDto generatorDto) throws SQLException, ClassNotFoundException, InterruptedException, IOException {

//       校验sys_是否符合规范
        this.checkTableName(generatorDto.getTableName());

//       检验表名称是否为系统中包含的数据库表
        this.checkTableNameInDatabase(generatorDto.getTableName());

        String sql = this.generatorSql(generatorDto);
//     生成数据库表
        this.generatorTable(generatorDto.getTableName(), sql);

//        生成代码方法start
//        克隆一个对象 对原始的对象值不污染

        GeneratorDto generatorDtoCreate = ObjectUtil.cloneByStream(generatorDto);
        //        表创建完毕以后，对所有字段进行转驼峰,保留原始的，回显的时候使用
        generatorDtoCreate.setDomains(generatorDtoCreate.getDomains().stream().map(sqlDto ->
                {
                    // todo 如果需要查询，则需要设置查询名和查询数据剋行
                    if (sqlDto.getIsSearch()) {
                        sqlDto.setSearchType(this.converTypeAndSizeToObjectType(sqlDto.getTypeAndsize()));
                        sqlDto.setSearchName(StrUtil.toCamelCase(sqlDto.getName()));
                    }
                    //todo 如果不需要属性，则需要设置值为默认值
                    if (!sqlDto.getIsSearch()) {
                        sqlDto.setSearchType(null);
                        sqlDto.setSearchName(null);
                    }

                    sqlDto.setName(StrUtil.toCamelCase(sqlDto.getName()));
                    if (StrUtil.isNotEmpty(sqlDto.getName())) {
                        String capitalized = StrUtil.upperFirst(sqlDto.getName()); // 首字母大写
                        sqlDto.setGMethod("get" + capitalized + "()"); // 设置 gMethod
                        sqlDto.setGLamda("get" + capitalized); // 设置 gMethod
                        sqlDto.setSMethod("set" + capitalized + "()"); // 设置 sMethod
                    }
                    return sqlDto; // 返回修改后的对象
                }
        ).collect(Collectors.toList()));

        this.generatorBackendCode(generatorDtoCreate.getTableName(), generatorDtoCreate.getDomains());
        this.generatorFrontCode(generatorDtoCreate);
//        保存的为原始用户提交的记录，方便回显
        this.generatorPermissons(generatorDto);

        this.saveRecord(generatorDto);
        this.documentGeneration();

    }


    /**
     * 检查数据库表是否在系统中
     * 如果在系统中那么将无法被替代
     *
     * @param tableName
     */
    private void checkTableNameInDatabase(String tableName) {
        if (dictService.list(
                new LambdaQueryWrapper<Dict>().eq(Dict::getName, tableName).ne(Dict::getPid, 0)
        ).size() > 0) {
            throw new RuntimeException(tableName + "为系统表,无法被覆盖，请修改表名称!");
        }
    }

    /**
     * 校验表是否符合规范
     *
     * @param tableName
     */
    private void checkTableName(String tableName) {
        if (!tableName.matches("^sys_[a-z]+$")) {
            // SQL语法错误，不是有效SQL
            throw new RuntimeException("表以 sys_ 开头，且后面只包含小写字母");
        }
    }

    /**
     * 保存创建记录
     *
     * @param
     */
    private void saveRecord(GeneratorDto generatorDto) {
        String sql = generatorSql(generatorDto);

        recordService.save(new Record().setSqlStr(sql).setGenerator(JSONUtil.toJsonStr(generatorDto)).setUid(StpUtil.getLoginIdAsLong()).setDescription(generatorDto.getDescription()).setTableName(generatorDto.getTableName()));
    }

    /**
     * 创建权限菜单数据信息
     *
     * @param generatorDto
     */
    private void generatorPermissons(GeneratorDto generatorDto) {
//        创建之前=先删除perssion的前两个开头记录
//        删除掉role的关联记录

        List<String> permsList = new
                ArrayList<>();
        permsList.add(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":" + "add");
        permsList.add(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":" + "update");
        permsList.add(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":" + "delete");
        permsList.add(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":" + "list");
        permsList.add(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":" + "search");
        permsList.add(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":" + ":batch:export");
        permsList.add(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":" + ":batch:upload");
        permsList.add(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":" + ":batch:delete");

        List<Long> pids = permissionService.list(new LambdaQueryWrapper<Permission>().in(Permission::getPerms, permsList)).
                stream().map(Permission::getId).collect(Collectors.toList());
        permissionService.removeBatchByIds(pids);
        if (pids.size() > 0) {
            rolePermissionService.remove(new LambdaQueryWrapper<RolePermission>().in(RolePermission::getPermissionId, pids));
        }
//      再开始创建
        Permission listPermission = new Permission().
                setParentId(0L).setComponent("/backend/" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + "/index")
                .setMenuType((byte) 2).setHidden(false).setTitle(generatorDto.getDescription()).
                setPath("/" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX)).setIcon("Menu")
                .setPerms("sys:" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":list");
        permissionService.save(listPermission);
        Permission addPermission = new Permission().setParentId(listPermission.getId())
                .setPerms(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":add").setMenuType((byte) 3).setTitle("新增");

        Permission searchPermission = new Permission().setParentId(listPermission.getId())
                .setPerms(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":search").setMenuType((byte) 3).setTitle("查询");
        Permission updatePermission = new Permission().setParentId(listPermission.getId())
                .setPerms(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":update").setMenuType((byte) 3).setTitle("修改");
        Permission exportPermission = new Permission().setParentId(listPermission.getId())
                .setPerms(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":batch:export").setMenuType((byte) 3).setTitle("批量导出");
        Permission batchdeletePermission = new Permission().setParentId(listPermission.getId())
                .setPerms(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":batch:upload").setMenuType((byte) 3).setTitle("批量导入");
        Permission batchuploadPermission = new Permission().setParentId(listPermission.getId())
                .setPerms(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":batch:delete").setMenuType((byte) 3).setTitle("批量删除");
        Permission deletePermission = new Permission().setParentId(listPermission.getId())
                .setPerms(StrUtil.removeSuffix(Constant.TABLE_PREFIX, "_") + ":" + StrUtil.removePrefix(generatorDto.getTableName(), Constant.TABLE_PREFIX) + ":delete").setMenuType((byte) 3).setTitle("删除");
        permissionService.save(addPermission);
        permissionService.save(searchPermission);
        permissionService.save(updatePermission);
        permissionService.save(deletePermission);
        permissionService.save(exportPermission);
        permissionService.save(batchuploadPermission);
        permissionService.save(batchdeletePermission);

    }


    /**
     * 创建前端代码
     * //todo 需要对参数格式格式进行驼峰转换
     *
     * @param generatorDto
     */
    private void generatorFrontCode(GeneratorDto generatorDto) throws IOException {


        //        设置velocity的资源加载器
        Properties properties = new Properties();
        properties.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        //        初始化velocity引擎
        Velocity.init(properties);
        //        创建velocity容器
        VelocityContext context = new VelocityContext();
        // 设置表单数据
        context.put("tableColumns", generatorDto.getDomains());
        //设置请求前缀
        context.put("tableName", StrUtil.removePrefix(generatorDto.getTableName(), "sys_"));
        //设置组件名称
        context.put("component", StrUtil.upperFirst(StrUtil.removePrefix(generatorDto.getTableName(), "sys_")));

        context.put("width", generatorDto.getWidth());

        //        加载velocity模版文件
        Template tpl = Velocity.getTemplate("templates/table.vue.vm", "UTF-8");
        //        设置输出路径
        log.info(System.getProperty("user.dir"));
        // 获取目标文件夹的路径
        String folderPath = System.getProperty("user.dir") + "/app_ui/src/views/backend/" + StrUtil.removePrefix(generatorDto.getTableName(), "sys_");
        // 创建File对象
        File folder = new File(folderPath);

        // 如果文件夹不存在，创建它
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created) {
                throw new RuntimeException("无法创建文件夹：" + folderPath);
            }
        }
        FileWriter fw = new FileWriter(System.getProperty("user.dir") + "/app_ui/src/views/backend/" + StrUtil.removePrefix(generatorDto.getTableName(), "sys_") + "/index.vue");
        //合并数据到模板
        tpl.merge(context, fw);
        //释放资源
        fw.close();
        log.info("前端vue代码生成成功");


    }

    /**
     * 构建后端代码
     * todo 覆盖的功能后续需要取消
     *
     * @param tableName
     */
    private void generatorBackendCode(String tableName, List<SqlDto> sqlDtoList) {


        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> {
                    builder
                            .disableOpenDir()
                            // 覆盖已生成文件
                            .enableSwagger()//启动swagger模式
                            .outputDir(System.getProperty("user.dir") + "/src/main/java/"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent(Constant.PACKAGE_NAME) // 设置父包名
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
                    builder.addInclude(tableName) // 设置需要生成的表名
                            .addTablePrefix(Constant.TABLE_PREFIX)
                            .serviceBuilder()
                            .formatServiceFileName("%sService")
                            .formatServiceImplFileName("%sServiceImpl")
                            .enableFileOverride()
                            .entityBuilder()
                            .enableLombok()


                            .addTableFills(new Column("create_time", FieldFill.INSERT))
                            .addTableFills(new Column("update_time", FieldFill.UPDATE))
                            .enableChainModel()
                            .logicDeleteColumnName("deleted")
                            .enableTableFieldAnnotation()
                            .enableFileOverride()
                            .controllerBuilder()
                            .enableRestStyle()
                            .formatFileName("%sController")
                            .enableFileOverride()
                            .mapperBuilder()
                            .enableBaseResultMap()  //生成通用的resultMap
                            .superClass(BaseMapper.class)
                            .formatMapperFileName("%sMapper")
                            .enableMapperAnnotation()
                            .formatXmlFileName("%sMapper")
                            .enableFileOverride();
                    // 设置过滤表前缀 可以在Constant自行设置
                })
                .injectionConfig(builder -> {
                    // 自定义属性
                    Map<String, Object> customMap = new HashMap<>();

                    customMap.put("columns", sqlDtoList);
                    customMap.put("hasJoin", sqlDtoList.stream()
                            .map(SqlDto::getEntityClass) // 获取 entityClass
                            .anyMatch(entityClass -> entityClass != null && !entityClass.isEmpty()));
                    // 将自定义属性放入 objectMap 中，使其在模板中可以访问
                    builder.customMap(customMap);
                })
                // 使用Velocity引擎模板
                .execute();
        log.info("后端SpringBoot代码生成成功");
    }


    /**
     * 基于sql创建table
     * v3.0.8创建table之前需要删除
     *
     * @param tableName
     * @param sql
     */
    private void generatorTable(String tableName, String sql) {

        try {
            DataSource ds = getDatasource();
            // 检查表是否存在的 SQL 查询，确保 table_schema 是当前数据库
            String checkTableExistsQuery = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ? AND table_schema = DATABASE()";
            // 传递 tableName 作为参数，防止 SQL 注入
            long count = Db.use(ds).queryNumber(checkTableExistsQuery, tableName).longValue();
            if (count > 0) {
                Db.use(ds).execute("DROP TABLE " + tableName);
                System.out.println("表 " + tableName + " 已删除。");
            } else {
                System.out.println("表 " + tableName + " 不存在。");
            }
            //创建数据库表
            Db.use(ds).execute(sql);
            log.info("数据库表生成成功");
        } catch (SQLException e) {
            throw new RuntimeException("构建数据库异常,详情为:" + e.getMessage());
        }
    }

    /**
     * 设置宽度
     *
     * @param generatorDto
     */
    private void formattedField(GeneratorDto generatorDto) {
        for (SqlDto sqlDto : generatorDto.getDomains()) {
            if ("textarea".equals(sqlDto.getFormComponent())) {
                generatorDto.setWidth("60%");
            }
        }

    }


    /**
     * 获取数据源
     *
     * @return
     */
    private DataSource getDatasource() {
        return new SimpleDataSource(url, username, password);
    }

    /**
     * 创建数据表基于sql语句
     *
     * @param generatorDto
     */
    private String generatorSql(GeneratorDto generatorDto) {

        //todo 需要根据表单的样式对表单的组件进行转换
        for (SqlDto sqlDto : generatorDto.getDomains()) {
            if ("slider".equals(sqlDto.getFormComponent()) || "rate".equals(sqlDto.getFormComponent()) || "number".equals(sqlDto.getFormComponent())) {
                sqlDto.setTypeAndsize("int(5)");
            }
            //todo 多图片上传处理
            if ("mutileimg".equals(sqlDto.getFormComponent())) {
                sqlDto.setTypeAndsize("longtext");
            }
            //todo 关联信息如果为空     则设置关联的属性也为空
            if (sqlDto.getRelevance() == null || sqlDto.getRelevance().length == 0) {
                sqlDto.setEntityClass(null);
                sqlDto.setObjectName(null);
                sqlDto.setColumnType(null);

            }
            //todo 在此处判断relevan的长度如果是两个则是数据字典,如果是一个则是关联表


            if (sqlDto.getRelevance() != null && sqlDto.getRelevance().length > 0) {
//                todo 对数据字典的处理
                if (sqlDto.getRelevance().length == 2) {
//               todo  类似于关联却又比关联多了一个dictId
                    //                    设置表单类型为input
                    sqlDto.setFormComponent("input");
//                   如果校验无问题，设置为bigint（20属性）
                    sqlDto.setTypeAndsize("bigint(20)");
//                   赋值关联实体类名
                    sqlDto.setEntityClass("Dict");
//                   赋值关联实体对象名
                    sqlDto.setObjectName(sqlDto.getName() + "Dict");
//                   赋值关联实体属性名
                    sqlDto.setColumnName("name");
//                   赋值关联属性的数据类型
                    sqlDto.setColumnType("Long");
                    sqlDto.setDictId(Long.valueOf(sqlDto.getRelevance()[1]));
                    sqlDto.setSetRMethod(StrUtil.upperFirst(sqlDto.getName()) + "Dict");
                } else if (sqlDto.getRelevance().length == 3) {


                    if (generatorDto.getTableName().equals(sqlDto.getRelevance()[1])) {
                        throw new RuntimeException("关联表不能是当前数据库表!");
                    } else {
//                    设置表单类型为input
                        sqlDto.setFormComponent("input");
//                   如果校验无问题，设置为bigint（20属性）
                        sqlDto.setTypeAndsize("bigint(20)");
//                   赋值关联实体类名
                        sqlDto.setEntityClass(StrUtil.upperFirst(StrUtil.removePrefix(sqlDto.getRelevance()[1], "sys_")));
//                   赋值关联实体对象名
                        sqlDto.setObjectName(StrUtil.removePrefix(sqlDto.getRelevance()[1], "sys_"));
//                   赋值关联实体属性名
                        sqlDto.setColumnName(ReUtil.replaceAll(sqlDto.getRelevance()[2], "^.*/", ""));
//                   赋值关联属性的数据类型
                        sqlDto.setColumnType(this.converTypeAndSizeToObjectType(ReUtil.replaceAll(sqlDto.getRelevance()[2], "/.*$", "")));
                        sqlDto.setSetRMethod(StrUtil.upperFirst(sqlDto.getEntityClass()));
                    }

                }


            }


        }
        // todo 使用流检查 entityClass 是否有重复，过滤掉 ""和 null
        Map<String, Long> entityClassCount = generatorDto.getDomains().stream()
                .map(SqlDto::getEntityClass) // 获取 entityClass
                .filter(entityClass -> entityClass != null && !entityClass.isEmpty()) // 过滤掉空和 null
                .collect(Collectors.groupingBy(entityClass -> entityClass, Collectors.counting()));
        // 检查是否有重复的 entityClass
        boolean hasDuplicates = entityClassCount.values().stream().anyMatch(count -> count > 1);
        if (hasDuplicates) {
            throw new RuntimeException("字段中存在相同的关联数据库表，请及时修改！");
        }
        // todo  创建数据库表,后续需要加入对表的校验
        String sql = "";
        sql += "CREATE TABLE `" + generatorDto.getTableName() + "` (\n";
        sql += "`id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '序列号',\n";
        for (SqlDto column : generatorDto.getDomains()) {
            String name = column.getName() != null ? column.getName() : null;
            String typeAndsize = column.getTypeAndsize() != null ? column.getTypeAndsize() : null;
            // todo 优化默认值异常
            String defaultValue = (column.getDefaultValue() == null || column.getDefaultValue().trim().isEmpty()) ? null : column.getDefaultValue();
            if ((column.getDefaultValue() == null || column.getDefaultValue().trim().isEmpty())) {
                column.setDefaultValue(null);
            }
            String description = column.getDescription() != null ? column.getDescription() : null;
            if (!"id".equals(column.getName())) {
                sql += "`" + name + "` " + typeAndsize + " COLLATE utf8mb4_unicode_ci DEFAULT " + defaultValue + " COMMENT '" + description + "',\n";
            }
        }
        sql += "PRIMARY KEY (`id`) USING BTREE \n";
        sql += ") ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='" + generatorDto.getDescription() + "';\n\n\n";

        return sql;
    }

    /**
     * 根据数据类型获取到后端的数据类型
     *
     * @param typeAndSize
     * @return
     */
    private String converTypeAndSizeToObjectType(String typeAndSize) {
        //根据typeAndSize判断实体属性数据类型
        String backendType = "";
        switch (typeAndSize) {
            case "varchar(255)":
                backendType = "String";
                break;
            case "int(1)":
            case "int(5)":
            case "int(10)":
                backendType = "Integer";
                break;
            case "bigint(20)":
                backendType = "Long";
                break;
            case "double(10.2)":
                backendType = "Double";
                break;
            case "datetime":
                backendType = "String"; // 或者使用 java.util.Date
                break;
            case "text":
            case "longtext":
                backendType = "String";
                break;
            default:
                backendType = "String"; // 默认使用 String 类型
                break;
        }
        return backendType;
    }

    /**
     * 生成数据库文档
     */
    private void documentGeneration() {


        //数据源
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(datasource);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        //设置可以获取tables remarks信息
        hikariConfig.addDataSourceProperty("useInformationSchema", "true");
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(5);
        DataSource dataSource = new HikariDataSource(hikariConfig);
        //生成配置
        EngineConfig engineConfig = EngineConfig.builder()
                //生成文件路径
                .fileOutputDir(path)
                //打开目录
                .openOutputDir(false)
                //文件类型
                .fileType(EngineFileType.WORD)
                //生成模板实现
                .produceType(EngineTemplateType.freemarker)
                //自定义文件名称
                .build();

        //忽略表
        ArrayList<String> ignoreTableName = new ArrayList<>();

        //忽略表前缀
        ArrayList<String> ignorePrefix = new ArrayList<>();

        //忽略表后缀
        ArrayList<String> ignoreSuffix = new ArrayList<>();

        ProcessConfig processConfig = ProcessConfig.builder()
                //指定生成逻辑、当存在指定表、指定表前缀、指定表后缀时，将生成指定表，其余表不生成、并跳过忽略表配置
                //根据名称指定表生成
                .designatedTableName(new ArrayList<>())
                //根据表前缀生成
                .designatedTablePrefix(new ArrayList<>())
                //根据表后缀生成
                .designatedTableSuffix(new ArrayList<>())
                //忽略表名
                .ignoreTableName(ignoreTableName)
                //忽略表前缀
                .ignoreTablePrefix(ignorePrefix)
                //忽略表后缀
                .ignoreTableSuffix(ignoreSuffix).build();
        //配置
        Configuration config = Configuration.builder()
                //版本
                .version(Constant.VERSION)
                //描述
                .description("数据库设计文档")
                //数据源
                .dataSource(dataSource)
                //生成配置
                .engineConfig(engineConfig)
                //生成配置
                .produceConfig(processConfig)
                .build();
        //执行生成
        new DocumentationExecute(config).execute();
    }

    /**
     * 根据sql语句生成动态表单
     *
     * @param generatorDto
     * @return
     */
    public GeneratorDto generatorBySql(GeneratorDto generatorDto) {

        String sql = generatorDto.getSql();


        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);

            if (!(stmt instanceof CreateTable)) {
                throw new RuntimeException("请确保输入的Sql语句为建表语句");
            } else {
                CreateTable createTable = (CreateTable) stmt;
                Table table = createTable.getTable();
                String tableName = table.getName().replace("`", "").trim();

                if (!tableName.matches("^sys_[a-z]+$")) {
                    // SQL语法错误，不是有效SQL
                    throw new RuntimeException("表以 sys_ 开头，且后面只包含小写字母");
                }

                // 查找 id 字段
                List<ColumnDefinition> columns = createTable.getColumnDefinitions();
                ColumnDefinition idColumn = columns.stream()
                        .filter(column -> {
                            String columnName = column.getColumnName().replace("`", "").trim();
                            return "id".equals(columnName);
                        })
                        .findFirst()
                        .orElse(null); // 或者使用 orElseThrow() 抛出异常表示未找到

                // 如果存在 id，则移除并重新插入到最前面
                if (idColumn != null) {
                    columns.remove(idColumn);
                    columns.add(0, idColumn);
                } else {
                    // 如果不存在 id，则创建一个新的 id 字段并插入到最前面
                    ColumnDefinition newIdColumn = new ColumnDefinition();
                    newIdColumn.setColumnName("id");
                    ColDataType dataType = new ColDataType();
                    dataType.setDataType("BIGINT");
                    dataType.setArgumentsStringList(List.of("20"));
                    newIdColumn.setColDataType(dataType);
                    newIdColumn.setColumnSpecs(List.of("NOT", "NULL", "AUTO_INCREMENT", "COMMENT '序号'"));
                    columns.add(0, newIdColumn);
                }

                // 使用 Stream 判断是否存在不在枚举中的字段类型
                boolean allMatch = columns.stream().allMatch(column -> {
                    ColDataType colDataType = column.getColDataType();
                    String columnType = colDataType.getDataType().toLowerCase(); // 统一转为小写
                    columnType = columnType.replaceAll("\\s+", "");
                    List<String> args = colDataType.getArgumentsStringList();

                    String normalizedType;
                    if (args != null && !args.isEmpty()) {
                        // 先 trim 掉每个参数里的空格，再拼接
                        List<String> trimmedArgs = args.stream()
                                .map(String::trim)
                                .collect(Collectors.toList());
                        // 不需要拼接参数的类型集合
                        Set<String> noArgsTypes = Set.of("text", "longtext", "datetime");

                        if (args != null && !args.isEmpty() && !noArgsTypes.contains(columnType.toLowerCase())) {
                            normalizedType = columnType + "(" + String.join("", args) + ")";
                        } else {
                            normalizedType = columnType;
                        }

// 去除空格
                        normalizedType = normalizedType.replaceAll("\\s+", "");
                    } else {
                        normalizedType = columnType;
                    }

                    try {
                        ColumnTypeEnum.fromValue(normalizedType); // 尝试匹配枚举
                        return true; // 匹配成功
                    } catch (IllegalArgumentException e) {
                        return false; // 匹配失败，表示该类型不存在于枚举中
                    }
                });
                //todo 后续开发改进  有计划做成支持所有类型和长度的

                if (!allMatch) {

                    throw new RuntimeException("存在不支持的字段类型和长度，系统暂时支持的为:" + Arrays.stream(ColumnTypeEnum.values())
                            .map(type -> type.getValue())
                            .collect(Collectors.joining("-")));
                }

                List<SqlDto> sqlDtos = columns.stream().map(column -> {
                    ColDataType colDataType = column.getColDataType();
                    String columnType = colDataType.getDataType().toLowerCase();
                    List<String> args = colDataType.getArgumentsStringList();

                    String normalizedType;
                    if (args != null && !args.isEmpty()) {
                        normalizedType = columnType + "(" + String.join("", args) + ")";
                    } else {
                        normalizedType = columnType;
                    }
                    SqlDto dto = new SqlDto();
                    // 使用 Hutool 为每个字段设置唯一值
                    dto.setKey(IdUtil.fastSimpleUUID());     // 主 key
                    dto.setKey1(IdUtil.fastSimpleUUID());
                    dto.setKey2(IdUtil.fastSimpleUUID());
                    dto.setKey3(IdUtil.fastSimpleUUID());
                    dto.setKey4(IdUtil.fastSimpleUUID());
                    dto.setKey5(IdUtil.fastSimpleUUID());
                    dto.setKey6(IdUtil.fastSimpleUUID());
                    dto.setKey7(IdUtil.fastSimpleUUID());
                    dto.setKey8(IdUtil.fastSimpleUUID());
                    dto.setKey9(IdUtil.fastSimpleUUID());
                    dto.setName(column.getColumnName().replace("`", "").trim());
                    dto.setTypeAndsize(normalizedType);
                    if (!dto.getName().equals("id")) {
                        dto.setIsRequire(true);
                        dto.setIsShow(true);
                    }
                    dto.setFormComponent("input");
                    if ("id".equals(dto.getName())) {
                        dto.setFormComponent("input");
                    }
                    List<String> specs = column.getColumnSpecs();
                    String defaultValue = IntStream.range(0, specs.size() - 1)
                            .filter(i -> "DEFAULT".equalsIgnoreCase(specs.get(i)))
                            .mapToObj(i -> specs.get(i + 1))
                            .findFirst()
                            .orElse("")
                            .replaceAll("^'(.*)'$", "$1") // 去除引号
                            .trim(); // 去空格，防止 " " 被赋值
                    if (ObjectUtil.isNotEmpty(defaultValue)) {
                        dto.setDefaultValue(defaultValue);
                    } else {
                        dto.setDefaultValue(null);
                    }
                    String description = IntStream.range(0, specs.size() - 1)
                            .filter(i -> "COMMENT".equalsIgnoreCase(specs.get(i)))
                            .mapToObj(i -> specs.get(i + 1))
                            .findFirst()
                            .orElse("")
                            .replaceAll("^'(.*)'$", "$1");
                    dto.setDescription(description);
                    return dto;
                }).collect(Collectors.toList());
                generatorDto.setTableName(tableName);
                List<String> options = createTable.getTableOptionsStrings();
                for (int i = 0; i < options.size(); i++) {
                    String item = options.get(i).toUpperCase();
                    if ("COMMENT".equals(item) && i + 2 < options.size()) {
                        String comment = options.get(i + 2);
                        // 去掉首尾单引号（如 '系统演示表' -> 系统演示表）
                        generatorDto.setDescription(comment.replaceAll("^'(.*)'$", "$1"));
                    }
                }
                generatorDto.setDomains(sqlDtos);
            }
        } catch (JSQLParserException e) {
            // SQL语法错误，不是有效SQL
            throw new RuntimeException("Sql语法错误");
        }
        generatorDto.setSql(null);
        return generatorDto;
    }

}