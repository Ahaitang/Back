package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileController {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        try {
            String url = saveFile(file);
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("name", file.getOriginalFilename());
            return Result.success(result);
        } catch (IOException e) {
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
                    String url = saveFile(file);
                    Map<String, String> item = new HashMap<>();
                    item.put("url", url);
                    item.put("name", file.getOriginalFilename());
                    results.add(item);
                } catch (IOException e) {
                    // 忽略失败的文件
                }
            }
        }

        return Result.success(results);
    }

    /**
     * 保存文件并返回访问URL
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 生成日期目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;

        // 创建目录
        File dir = new File(uploadPath + "/" + dateDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 保存文件
        File destFile = new File(dir, newFilename);
        file.transferTo(destFile);

        // 返回相对URL
        return "/uploads/" + dateDir + "/" + newFilename;
    }
}