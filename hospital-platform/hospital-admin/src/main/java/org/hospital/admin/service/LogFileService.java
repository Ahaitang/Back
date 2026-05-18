package org.hospital.admin.service;

import lombok.extern.slf4j.Slf4j;
import org.hospital.admin.model.LogFileInfo;
import org.hospital.admin.model.LogReadResponse;
import org.hospital.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 日志文件读取服务
 */
@Slf4j
@Service
public class LogFileService {

    private static final int BAD_REQUEST = 400;

    @Value("${logging.file.path:logs}")
    private String logPath;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 列出所有日志文件
     */
    public List<LogFileInfo> listLogFiles() {
        Path logDir = getLogDirectory();
        if (!Files.exists(logDir) || !Files.isDirectory(logDir)) {
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.list(logDir)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith("hospital-platform") && p.getFileName().toString().endsWith(".log"))
                    .map(this::toLogFileInfo)
                    .sorted(Comparator.comparing(LogFileInfo::getName))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("列出日志文件失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 分页读取日志内容
     */
    public LogReadResponse readLogLines(String filename, int page, int pageSize, String keyword, String level) {
        Path filePath = resolveAndValidate(filename);
        LogReadResponse response = new LogReadResponse();

        try {
            List<String> allLines = Files.readAllLines(filePath);
            List<String> filtered = allLines;

            // 级别过滤
            if (level != null && !level.isEmpty() && !"ALL".equalsIgnoreCase(level)) {
                String levelUpper = level.toUpperCase();
                filtered = filtered.stream()
                        .filter(line -> line.contains(" " + levelUpper + " ") || line.contains(" " + levelUpper + "  "))
                        .collect(Collectors.toList());
            }

            // 关键字搜索
            if (keyword != null && !keyword.isEmpty()) {
                String kw = keyword.toLowerCase();
                filtered = filtered.stream()
                        .filter(line -> line.toLowerCase().contains(kw))
                        .collect(Collectors.toList());
            }

            long totalLines = filtered.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, filtered.size());

            if (startIndex >= filtered.size()) {
                response.setLines(Collections.emptyList());
            } else {
                response.setLines(filtered.subList(startIndex, endIndex));
            }

            response.setTotalLines(totalLines);
            response.setCurrentPage(page);
            response.setHasMore(endIndex < filtered.size());
        } catch (IOException e) {
            log.error("读取日志文件失败: {}", filename, e);
            throw new BusinessException(BAD_REQUEST, "读取日志文件失败: " + e.getMessage());
        }

        return response;
    }

    /**
     * 获取文件输入流（用于下载）
     */
    public InputStream getFileInputStream(String filename) {
        Path filePath = resolveAndValidate(filename);
        try {
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new BusinessException(BAD_REQUEST, "无法读取日志文件: " + filename);
        }
    }

    /**
     * 获取文件大小
     */
    public long getFileSize(String filename) {
        Path filePath = resolveAndValidate(filename);
        try {
            return Files.size(filePath);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * 获取日志目录路径
     */
    public Path getLogDirectory() {
        Path path = Paths.get(logPath);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.dir")).resolve(path);
        }
        return path;
    }

    /**
     * 安全解析文件名并验证路径（防止路径遍历）
     */
    public Path resolveAndValidate(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new BusinessException(BAD_REQUEST, "文件名不能为空");
        }
        // 禁止路径分隔符和父目录引用
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BusinessException(BAD_REQUEST, "非法文件名");
        }
        // 校验文件名格式
        if (!filename.startsWith("hospital-platform") || !filename.endsWith(".log")) {
            throw new BusinessException(BAD_REQUEST, "不允许访问该文件");
        }

        Path logDir = getLogDirectory();
        Path filePath = logDir.resolve(filename).normalize();

        // 确保解析后的路径仍在日志目录内
        if (!filePath.startsWith(logDir)) {
            throw new BusinessException(BAD_REQUEST, "非法文件路径");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new BusinessException(BAD_REQUEST, "文件不存在: " + filename);
        }

        return filePath;
    }

    private LogFileInfo toLogFileInfo(Path path) {
        LogFileInfo info = new LogFileInfo();
        info.setName(path.getFileName().toString());
        try {
            info.setSize(Files.size(path));
            Instant modified = Files.getLastModifiedTime(path).toInstant();
            info.setLastModified(LocalDateTime.ofInstant(modified, ZoneId.systemDefault()).format(FORMATTER));
        } catch (IOException e) {
            info.setSize(0L);
            info.setLastModified("");
        }
        return info;
    }
}
