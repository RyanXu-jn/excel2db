package com.ryantsui.controller;

import com.ryantsui.entity.JsonMessage;
import com.ryantsui.utils.FileManagerUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件上传控制.
 */
@RestController
@RequestMapping(value = "/file/common")
public class FileController {

    @RequestMapping("/imageUpload")
    public JsonMessage imageUpload(MultipartFile file) throws Exception {
        String path = FileManagerUtil.upload(file, "");
        return new JsonMessage().success(path);
    }

    @RequestMapping("/videoUpload")
    public JsonMessage videoUpload(MultipartFile file) throws Exception {
        String path = FileManagerUtil.upload(file, "");
        return new JsonMessage().success(path);
    }

    @RequestMapping("/ckeditorUpload")
    public Map<String, Object> ckeditorUpload(MultipartFile file) throws Exception {
        Map<String, Object> ckEditor = new HashMap<>();
        String path = FileManagerUtil.upload(file, "");
        ckEditor.put("uploaded", true);
        ckEditor.put("url", path);
        return ckEditor;
    }

    /**
     * 断点续传上传.
     * @param file file
     * @return json
     * @throws Exception exception
     */
    @RequestMapping("/videoTusUpload")
    public JsonMessage videoTusUpload(MultipartFile file) throws Exception {
        String path = FileManagerUtil.tusFileUpload(file, "");
        return new JsonMessage().success(path);
    }

    /**
     * 删除文件通用方法.
     * @param path 路径
     * @return json
     * @throws Exception 异常
     */
    @RequestMapping("/removeUploadedFile")
    public JsonMessage removeUploadedFile(String path) throws Exception {
        FileManagerUtil.delete(path);
        return new JsonMessage().success();
    }
}
