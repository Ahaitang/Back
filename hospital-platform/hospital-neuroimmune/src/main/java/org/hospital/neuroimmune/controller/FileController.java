package org.hospital.neuroimmune.controller;

import org.hospital.common.model.Result;
import org.hospital.neuroimmune.service.MinioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/v1/neuroimmune/file")
@CrossOrigin
public class FileController {

    @Autowired
    private MinioService minioService;

    // 允许上传的文件类型白名单
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "bmp",  // 图片
        "pdf", "doc", "docx", "xls", "xlsx",  // 文档
        "txt", "csv"                          // 文本
    );

    // 最大文件大小 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        // 文件大小检查
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小超过限制（最大10MB）");
        }

        // 文件类型检查
        String filename = file.getOriginalFilename();
        if (filename == null || !isAllowedFileType(filename)) {
            return Result.error("文件类型不支持，仅支持：图片(jpg/png/gif)、文档(pdf/doc/xls)、文本(txt/csv)");
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
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                errors.add("文件为空: " + file.getOriginalFilename());
                continue;
            }

            // 文件大小检查
            if (file.getSize() > MAX_FILE_SIZE) {
                errors.add("文件过大: " + file.getOriginalFilename());
                continue;
            }

            // 文件类型检查
            String filename = file.getOriginalFilename();
            if (filename == null || !isAllowedFileType(filename)) {
                errors.add("类型不支持: " + filename);
                continue;
            }

            try {
                String url = minioService.uploadFile(file);
                Map<String, String> item = new HashMap<>();
                item.put("url", url);
                item.put("name", file.getOriginalFilename());
                results.add(item);
            } catch (Exception e) {
                errors.add("上传失败: " + filename + " - " + e.getMessage());
            }
        }

        if (!errors.isEmpty() && results.isEmpty()) {
            return Result.error("所有文件上传失败: " + errors.get(0));
        }

        return Result.success(results);
    }

    /**
     * 检查文件类型是否允许
     */
    private boolean isAllowedFileType(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return false;
        }
        String extension = filename.substring(lastDot + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}