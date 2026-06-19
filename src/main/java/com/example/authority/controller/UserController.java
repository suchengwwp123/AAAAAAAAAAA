package com.example.authority.controller;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.TokenSign;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.authority.common.Constant;
import com.example.authority.common.Result;
import com.example.authority.entity.*;
import com.example.authority.annotations.ClearPerms;
import com.example.authority.entity.dto.OnlineUserDTO;
import com.example.authority.enums.Subscription;
import com.example.authority.handler.WebSocketHandler;
import com.example.authority.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @program: design
 * @ClassName:UserController
 * @description: UserController前端控制器
 * @author:dyy
 * @Version 3.0
 **/
@Tag(name = "用户表 前端控制器")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private UserRoleService userRoleService;
    @Autowired
    private HttpSession httpSession;
    @Resource
    private CollectService collectService;
    @Resource
    private VitaeService  vitaeService;
    @Resource
    private OrderService orderService;
    @Resource
    private TicketsService  ticketsService;
    @Resource
    private ComplainService complainService;



    /**
     * 新增
     *
     * @param user
     * @return
     */
    @Operation(summary = "新增")
    @PostMapping
    public Result save(@RequestBody User user) {
        if (userService.exists(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()))) {
            return Result.error("此账户名已经存在，请更换！");
        }
        if (userService.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()))) {
            return Result.error("此邮箱已经存在，请更换！");
        }
        if (userService.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone()))) {
            return Result.error("此手机号已经存在，请更换！");
        }
        user.setPassword(passwordEncoder.encode(Constant.DEFAULT_PASSWORD));
        userService.save(user);
        this.saveBatchUserRoles(user);

        return Result.success();
    }

    /**
     * 批量新增userRole
     *
     * @param user
     */
    private void saveBatchUserRoles(User user
    ) {
        user.getRoleIds().forEach(rid -> {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(rid);
            userRoleService.save(userRole);
        });
    }

    /**
     * 修改
     *
     * @param user
     * @return
     */
    @Operation(summary = "修改")
    @PutMapping("/{id}")
    @ClearPerms
    public Result update(@PathVariable Long id, @RequestBody User user) {


        if (userService.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()).ne(User::getId, id))) {
            return Result.error("此邮箱已经存在，请更换！");
        }
        if (userService.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone()).ne(User::getId, id))) {
            return Result.error("此手机号已经存在，请更换！");
        }

//        先删除掉此用户所有的roleIds，然后再添加前端传递过来的数据、
        userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        this.saveBatchUserRoles(user);
        User queryUser = userService.getById(id);
        if (user.getStatu() != queryUser.getStatu() && user.getStatu() == 0) {
            StpUtil.logout(id);
            WebSocketHandler.broadcastToChannel(Subscription.ONLINEUSER_TAKE.name(), JSONUtil.toJsonStr(SaResult.ok()));
        }
        return Result.success(userService.updateById(user));
    }

    /**
     * 修改个人信息
     *
     * @param user
     * @return
     */
    @ClearPerms(value = true)
    @Operation(summary = "修改个人信息")
    @PutMapping
    public Result updateInformation(@RequestBody User user) {
        if (userService.exists(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()).ne(User::getId, StpUtil.getLoginIdAsLong()))) {
            return Result.error("此邮箱已经存在，请更换！");
        }
        if (userService.exists(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone()).ne(User::getId, StpUtil.getLoginIdAsLong()))) {
            return Result.error("此手机号已经存在，请更换！");
        }
        return Result.success(userService.updateById(user));
    }

    /**
     * 查询所有User
     *
     * @return
     */
    @Operation(summary = "查询所有User")

    @GetMapping
    public Result indAll() {
        LambdaQueryWrapper<UserRole> userRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<UserRole> userRoles = userRoleService.list(userRoleLambdaQueryWrapper.eq(UserRole::getRoleId, 1L));
        if (userRoles.size()==0){
            return Result.success(new Page<User>());
        }

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getId);
        lambdaQueryWrapper.in(User::getId,userRoles.stream().map(UserRole::getUserId).collect(Collectors.toList()));

        return Result.success(userService.list(lambdaQueryWrapper));
    }

    /**
     * 获取单个
     *
     * @param id
     * @return
     */
    @Operation(summary = "获取单个")
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        User user = userService.getById(id);
        user.setRoleIds(userRoleService.list(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getId())).stream().map(UserRole::getRoleId).collect(Collectors.toList()));
        return Result.success(user);
    }

    /**
     * 分页显示
     *
     * @param name
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Operation(summary = "分页显示")
    @GetMapping("/page")
    public Result findPage(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String phone,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "") Integer statu,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<UserRole> userRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<UserRole> userRoles = userRoleService.list(userRoleLambdaQueryWrapper.in(UserRole::getRoleId, 1L,6L));
        if (userRoles.size()==0){
            return Result.success(new Page<User>());
        }

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<User>().like(User::getNickname, name).orderByDesc(User::getId);
        lambdaQueryWrapper.in(User::getId,userRoles.stream().map(UserRole::getUserId).collect(Collectors.toList()));

        return Result.success(userService.page(new Page<>(pageNum, pageSize), lambdaQueryWrapper));
    }

    @Operation(summary = "分页显示")
    @GetMapping("/pages")
    public Result findSidPage(
            @RequestParam(defaultValue = "") String name,

            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<UserRole> userRoleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        List<UserRole> userRoles = userRoleService.list(userRoleLambdaQueryWrapper.eq(UserRole::getRoleId, 7L));
        if (userRoles.size()==0){
            return Result.success(new Page<User>());
        }

        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<User>().like(User::getNickname, name).orderByDesc(User::getId);
        lambdaQueryWrapper.in(User::getId,userRoles.stream().map(UserRole::getUserId).collect(Collectors.toList()));

        return Result.success(userService.page(new Page<>(pageNum, pageSize), lambdaQueryWrapper));
    }

    /**
     * 在线用户分页
     *
     * @param name
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Operation(summary = "在线用户分页")
    @GetMapping("/onlinepage")
    public Result onlinePage(
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(User::getUsername, name).orderByDesc(User::getId);

        // 获取所有已登录的 session ID
        List<String> sessionIds = StpUtil.searchSessionId("", 0, -1, false);
        List<SaSession> httpSessions = new ArrayList<>();
        List<Object> loginIds = sessionIds.stream().map(sessionId -> {
            SaSession session = StpUtil.getSessionBySessionId(sessionId);
            httpSessions.add(session);
            return session.getLoginId();
        }).toList();

        List<User> users;
        if (!loginIds.isEmpty()) {
            users = userService.list(queryWrapper.in(User::getId, loginIds));
        } else {
            users = new ArrayList<>();
        }

        // 过滤出有效的在线会话
        List<SaSession> filteredSessions = httpSessions.stream()
                .filter(session -> session.getLoginId() != null &&
                        users.stream().anyMatch(user -> user.getId().equals(session.getLoginId())))
                .collect(Collectors.toList());
//        log.warn(JSONUtil.toJsonStr(filteredSessions));
        // 分页处理
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, filteredSessions.size());

        List<SaSession> pagedSessions = filteredSessions.subList(start, end);

        // 转换成 DTO 列表
        List<OnlineUserDTO> dtoList = pagedSessions.stream().map(session -> {
                    User user = users.stream()
                            .filter(u -> u.getId().equals(session.getLoginId()))
                            .findFirst()
                            .orElse(null);

                    OnlineUserDTO dto = new OnlineUserDTO();
                    dto.setDevice(
                            session.getTokenSignList().stream()
                                    .map(TokenSign::getDevice)
                                    .findFirst()
                                    .orElse("")
                    );
                    dto.setId(session.getId());
                    if (user != null) {
                        dto.setLoginId(user.getId());
                        dto.setUsername(user.getUsername());
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


// 在设置字段时转换时间
                    dto.setLoginTime(
                            LocalDateTime.ofInstant(
                                    Instant.ofEpochMilli(session.getCreateTime()),
                                    ZoneId.systemDefault()
                            ).format(formatter)
                    );
                    dto.setTokenValue(session.getId());
                    return dto;
                }).sorted(Comparator.comparing(OnlineUserDTO::getLoginTime).reversed())  // 升序
                .collect(Collectors.toList());

        // 构建分页响应
        Page<OnlineUserDTO> page = new Page<>();
        page.setCurrent(pageNum);
        page.setSize(pageSize);
        page.setTotal(filteredSessions.size());
        page.setRecords(dtoList);

        return Result.success(page);
    }


    /**
     * 单个删除
     *
     * @param id
     * @return
     */
    @Operation(summary = "单个删除")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        removeUserAndLogout(id);
        return Result.success(userService.removeById(id));
    }

    /**
     * 删除和下线用户
     *
     * @param id
     */
    private void removeUserAndLogout(Object id) {
        userRoleService.remove(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        collectService.remove(new LambdaUpdateWrapper<Collect>().in(Collect::getUserId, Arrays.asList(id)));
        orderService.remove(new LambdaUpdateWrapper<Order>().in(Order::getUserId, Arrays.asList(id)));
        orderService.remove(new LambdaUpdateWrapper<Order>().in(Order::getWorkerId, Arrays.asList(id)));
        vitaeService.remove(new LambdaUpdateWrapper<Vitae>().in(Vitae::getUserId, Arrays.asList(id)));
        complainService.remove(new LambdaUpdateWrapper<Complain>().in(Complain::getUserId, Arrays.asList(id)));
        ticketsService.remove(new LambdaUpdateWrapper<Tickets>().in(Tickets::getUserId, Arrays.asList(id)));
        StpUtil.logout(id, "PC");
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("/batch/{ids}")
    @Transactional
    public Result deleteByIds(@PathVariable String[] ids) {
        Arrays.asList(ids).forEach(id -> removeUserAndLogout(id));
        return Result.success(userService.removeByIds(Arrays.asList(ids)));
    }

    /**
     * 批量导出
     *
     * @param ids
     * @param response
     * @throws IOException
     */
    @Operation(summary = "批量导出")
    @GetMapping("/batch/export/{ids}")
    public void exportByIds(@PathVariable String[] ids, HttpServletResponse response) throws IOException {
        List<User> list = userService.listByIds(Arrays.asList(ids));
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("user导出数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), User.class).sheet("sheel1").doWrite(list);

    }
}

