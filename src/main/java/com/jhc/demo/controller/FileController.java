package com.jhc.demo.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jhc.demo.common.Result;
import com.jhc.demo.entity.Files;
import com.jhc.demo.entity.User;
import com.jhc.demo.mapper.FileMapper;
import com.jhc.demo.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

//文件上传相关接口
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Resource
    private FileMapper fileMapper;

    /**
     * 上传文件
     *
     * @param file
     * @return
     * @throws IOException
     */
    @CrossOrigin(origins = "http://localhost:3333")
    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);
        long size = file.getSize();

        //定义一个文件的唯一标识码
        String uuid = IdUtil.fastSimpleUUID();
        String fileUUID = uuid + StrUtil.DOT + type;
        File uploadFile = new File(fileUploadPath + fileUUID);
        //判断配置的文件目录是否存在，若不存在则创建一个文件目录
        if (!uploadFile.getParentFile().exists()) {
            uploadFile.getParentFile().mkdirs();
        }

        String md5;
        String url;
        // 上传文件到磁盘
        file.transferTo(uploadFile);
        // 获取文件的md5, 不放到磁盘获取不了？
        md5 = SecureUtil.md5(uploadFile);
        // 从数据库查询是否存在相同的md5
        Files dbFiles = getFileByMd5(md5);
        if (dbFiles != null) {
            url = dbFiles.getUrl();
            // 由于文件已存在，所以删除刚才重复上传的文件
            uploadFile.delete();
        } else {
            url = "http://localhost:9091/file/" + fileUUID;
        }

        // 存储到数据库
        Files save_file = new Files();
        save_file.setName(originalFilename);
        save_file.setSize(size / 1024);
        save_file.setType(type);
        save_file.setUrl(url);
        save_file.setMd5(md5);
        User currentUser = TokenUtils.getCurrentUser();
        System.out.println("!!!!!!!" + currentUser.toString());
        save_file.setOwner(currentUser.getNickname());
        fileMapper.insert(save_file);
        return url;
    }

    /**
     * 文件下载路径 "http://localhost:9091/file/{fileUUID}";
     *
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        //根据文件的唯一标识获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);
        //设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
        response.setContentType("application/octet-stream");

        //读取文件的字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

    /**
     * + enter 快速生成注释
     * /**
     * 通过md5查询文件
     *
     * @param md5
     * @return
     */
    private Files getFileByMd5(String md5) {
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5", md5);
        List<Files> fileList = fileMapper.selectList(queryWrapper);
        return fileList.size() == 0 ? null : fileList.get(0);
    }

    /**
     * 分页查询所有文件
     *
     * @param currentPage
     * @param size
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer currentPage,
                           @RequestParam Integer size,
                           @RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String owner) {
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", false);  //查询未删除的记录
        queryWrapper.eq("enable", true);   //查询已启用的文件
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }

        if (!"".equals(owner)) {
            queryWrapper.like("owner", owner);
        }

        return Result.success(fileMapper.selectPage(new Page<>(currentPage, size), queryWrapper));
    }

    /**
     * 分页查询我的文件
     *
     * @param currentPage
     * @param size
     * @param name
     * @return
     */
    @GetMapping("/my_page")
    public Result findMyPage(@RequestParam Integer currentPage,
                             @RequestParam Integer size,
                             @RequestParam(defaultValue = "") String name) {
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_delete", false);  //查询未删除的记录
        User currentUser = TokenUtils.getCurrentUser();
        queryWrapper.eq("owner", currentUser.getNickname());  //查询自己上传的文件
        queryWrapper.orderByDesc("id");
        if (!"".equals(name)) {
            queryWrapper.like("name", name);
        }

        return Result.success(fileMapper.selectPage(new Page<>(currentPage, size), queryWrapper));
    }

    /**
     * 启用禁用文件
     *
     * @param id
     * @return
     */
    @GetMapping("/changeEnable/{id}")
    public Result changeEnable(@PathVariable Integer id) {
        Files files = fileMapper.selectById(id);
        files.setEnable(!files.getEnable());
        fileMapper.updateById(files);
        return Result.success();
    }

    /**
     * 删除接口
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        Files files = fileMapper.selectById(id);
        files.setIsDelete(true);
        fileMapper.updateById(files);
        return Result.success();
    }

    /**
     * 删除所有
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        QueryWrapper<Files> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<Files> filesList = fileMapper.selectList(queryWrapper);
        for (Files file : filesList) {
            file.setIsDelete(true);
            fileMapper.updateById(file);
        }

        return Result.success();
    }
}