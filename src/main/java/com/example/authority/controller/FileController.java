package com.example.authority.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.authority.common.Result;
import com.example.authority.entity.Files;
import com.example.authority.mapper.FileMapper;
import com.example.authority.service.FileService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: authority-2026.0.3
 * @ClassName:FileController
 * @description: 文件上传控制器
 * @author:dyy
 * @Version 3.0
 **/
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${files.upload.path}")
    //将application.yml文件中的文件位置赋予到fileUploadPath    //注意导包：.beans.factory.annotation.Value;
    private String fileUploadPath;
    @Value(("${files.upload.urlprefix}"))
    private String fileUploadUrl;
    @Value(("${files.upload.outputquality}"))
    private Double outputQuality;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private FileService fileService;

    /**
     * 上传文件
     *
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename(); // 原始文件名
        String type = FileUtil.extName(originalFilename);     // 文件扩展名
        long size = file.getSize();                           // 文件大小

        // 1. 定义唯一文件名
        String uuid = IdUtil.fastSimpleUUID();
        String fileUUID = uuid + StrUtil.DOT + type;
        File uploadFile = new File(fileUploadPath + fileUUID);

        // 2. 创建文件目录（如果不存在）
        if (!uploadFile.getParentFile().exists()) {
            uploadFile.getParentFile().mkdirs();
        }

        // 3. 图片压缩处理或直接保存
        if (type.equalsIgnoreCase("png") || type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase("jpeg")) {
            try (InputStream inputStream = file.getInputStream();
                 OutputStream outputStream = new FileOutputStream(uploadFile)) {
                Thumbnails.of(inputStream)
                        .scale(1.0)            // 保持原图尺寸
                        .outputQuality(outputQuality)    // 压缩质量 70%
                        .toOutputStream(outputStream);
            }
        } else {
            // 非图片文件直接保存
            file.transferTo(uploadFile);
        }

        // 4. 获取文件 md5 用于查重
        String md5 = SecureUtil.md5(uploadFile);
        Files dbFiles = getFileByMd5(md5);  // 根据 md5 查找已有文件记录
        String url;
        if (dbFiles != null) {
            url = dbFiles.getUrl();
            // 删除本地重复文件
            uploadFile.delete();
        } else {
            // 新文件构建访问地址
            url = fileUploadUrl + fileUUID;
        }
        // 5. 保存文件信息到数据库
        Files saveFile = new Files();
        saveFile.setName(originalFilename);
        saveFile.setType(type);
        saveFile.setSize(size / 1024);  // KB 单位
        saveFile.setUrl(url);
        saveFile.setMd5(md5);
        fileService.save(saveFile);
        return Result.success(url);
    }


    /**
     * 文件下载
     *
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        // 根据文件的唯一标识码获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);

        // 设置输出流格式
        ServletOutputStream os = response.getOutputStream(); // 写出流
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
        response.setContentType("application/octet-stream");

        // 读取文件的字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

    /**
     * md5查询文件
     *
     * @param md5
     * @return
     */
    private Files getFileByMd5(String md5) {
        // 查询文件的md5是否存在

        List<Files> filesList = fileMapper.selectList(new LambdaQueryWrapper<Files>().eq(Files::getMd5, md5));
        return filesList.size() == 0 ? null : filesList.get(0);
    }

    /**
     * 分页显示
     *
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")

    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {


        return Result.success(fileMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<Files>().eq(Files::getIsDelete, false).like(Files::getName, name).orderByDesc(Files::getId)));

    }

    /**
     * 获取全部文件
     *
     * @param
     * @param
     * @param
     * @return
     */
    @GetMapping

    public Result findALl() {


        return Result.success(fileService.list());

    }

    /**
     * 修改文件
     *
     * @param files
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody Files files) {
        return Result.success(
                fileService.saveOrUpdate(files));
    }

    /**
     * 删除文件
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        Files files = fileService.getById(id);
        files.setIsDelete(true);
        fileService.saveOrUpdate(files);
        return Result.success();
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/batch/{ids}")
    public Result deleteBatch(@PathVariable Long[] ids) {

        fileService.list(new LambdaQueryWrapper<Files>().in(Files::getId, ids)).forEach(files -> {
            files.setIsDelete(true);
            fileService.saveOrUpdate(files);
        });

        return Result.success();
    }


}





