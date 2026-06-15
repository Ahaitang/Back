package org.hospital.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.admin.model.LogFileInfo;
import org.hospital.admin.model.LogReadResponse;
import org.hospital.admin.service.LogFileService;
import org.hospital.common.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 服务器日志管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/super-admin/logs")
@CrossOrigin
public class LogController {

    @Autowired
    private LogFileService logFileService;

    /**
     * 获取日志文件列表
     */
    @GetMapping("/files")
    public Result<List<LogFileInfo>> listFiles() {
        List<LogFileInfo> files = logFileService.listLogFiles();
        return Result.success(files);
    }

    /**
     * 分页读取日志内容
     */
    @GetMapping("/read")
    public Result<LogReadResponse> readLog(
            @RequestParam String filename,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "200") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level) {
        LogReadResponse response = logFileService.readLogLines(filename, page, pageSize, keyword, level);
        return Result.success(response);
    }

    /**
     * 下载日志文件
     */
    @GetMapping("/download/{filename}")
    public ResponseEntity<InputStreamResource> download(@PathVariable String filename) {
        InputStream inputStream = logFileService.getFileInputStream(filename);
        long fileSize = logFileService.getFileSize(filename);
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(fileSize)
                .body(new InputStreamResource(inputStream));
    }
}
