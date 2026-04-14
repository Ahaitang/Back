package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/neuroimmune/file")
@CrossOrigin
public class FileController {

    @Autowired
    private MinioService minioService;

    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        try {
            String url = minioService.uploadFile(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("name", file.getOriginalFilename());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传多个文件
     */
    @PostMapping("/upload-batch")
    public Result<List<Map<String, String>>> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, String>> results = new ArrayList<>();

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String url = minioService.uploadFile(file);
                    Map<String, String> item = new HashMap<>();
                    item.put("url", url);
                    item.put("name", file.getOriginalFilename());
                    results.add(item);
                } catch (Exception e) {
                    // 忽略失败的文件
                }
            }
        }

        return Result.success(results);
    }
}