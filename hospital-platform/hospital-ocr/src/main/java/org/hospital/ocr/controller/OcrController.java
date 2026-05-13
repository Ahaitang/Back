package org.hospital.ocr.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.common.model.Result;
import org.hospital.ocr.dto.OcrResult;
import org.hospital.ocr.service.BaiduOcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OCR 病历识别接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ocr")
@CrossOrigin
public class OcrController {

    private static final long MAX_DOWNLOAD_SIZE = 10 * 1024 * 1024;

    @Autowired(required = false)
    private BaiduOcrService ocrService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OCR服务正常运行");
    }

    /**
     * 解析病历图片（从URL列表）
     * @param request 包含图片URL列表的请求
     * @return 解析结果
     */
    @PostMapping("/parse-medical")
    public Result<Map<String, Object>> parseMedical(@RequestBody Map<String, List<String>> request) {
        List<String> images = request.get("images");

        if (images == null || images.isEmpty()) {
            return Result.error("请提供图片URL");
        }

        if (ocrService == null) {
            return Result.error("OCR服务未配置");
        }

        try {
            StringBuilder allText = new StringBuilder();
            List<String> errors = new ArrayList<>();

            for (int i = 0; i < images.size(); i++) {
                String imageUrl = images.get(i);
                try {
                    byte[] imageData = downloadImage(imageUrl);
                    if (imageData == null || imageData.length == 0) {
                        errors.add("第 " + (i + 1) + " 张图片下载失败");
                        continue;
                    }

                    MultipartFile tempFile = createTempFile(imageData, imageUrl);

                    OcrResult result = ocrService.accurateBasic(tempFile);

                    if (result != null && result.isSuccess()) {
                        if (allText.length() > 0) {
                            allText.append("\n\n--- 第 " + (i + 1) + " 张图片 ---\n\n");
                        }
                        allText.append(result.getFullText());
                    } else {
                        errors.add("第 " + (i + 1) + " 张图片识别失败");
                    }
                } catch (Exception e) {
                    log.warn("处理OCR图片失败: index={}", i + 1, e);
                    errors.add("第 " + (i + 1) + " 张图片处理失败");
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("success", allText.length() > 0);
            data.put("content", allText.toString());
            if (!errors.isEmpty()) {
                data.put("errorMsg", errors.toString());
            }

            return Result.success(data);
        } catch (Exception e) {
            log.error("OCR解析失败", e);
            return Result.error("解析失败，请稍后重试");
        }
    }

    /**
     * 从URL下载图片
     */
    private byte[] downloadImage(String imageUrl) {
        HttpURLConnection connection = null;
        try {
            URL url = validateImageUrl(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);

            if (connection.getResponseCode() != 200) {
                log.error("下载图片失败，响应码: {}", connection.getResponseCode());
                return null;
            }

            String contentType = connection.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                log.warn("OCR图片下载被拒绝，Content-Type不支持: {}", contentType);
                return null;
            }

            long contentLength = connection.getContentLengthLong();
            if (contentLength > MAX_DOWNLOAD_SIZE) {
                log.warn("OCR图片下载被拒绝，文件过大: {} bytes", contentLength);
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (InputStream is = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    if (baos.size() + len > MAX_DOWNLOAD_SIZE) {
                        log.warn("OCR图片下载被拒绝，流大小超过限制");
                        return null;
                    }
                    baos.write(buffer, 0, len);
                }
            }

            return baos.toByteArray();
        } catch (IllegalArgumentException e) {
            log.warn("OCR图片URL被拒绝: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("下载OCR图片异常", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private URL validateImageUrl(String imageUrl) throws Exception {
        URI uri = URI.create(imageUrl);
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("仅支持HTTP/HTTPS图片地址");
        }
        if (uri.getUserInfo() != null) {
            throw new IllegalArgumentException("图片地址不能包含用户信息");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("图片地址缺少主机名");
        }
        InetAddress[] addresses = InetAddress.getAllByName(host);
        for (InetAddress address : addresses) {
            if (address.isAnyLocalAddress()
                    || address.isLoopbackAddress()
                    || address.isLinkLocalAddress()
                    || address.isSiteLocalAddress()
                    || address.isMulticastAddress()) {
                throw new IllegalArgumentException("图片地址不可访问内部网络");
            }
        }
        return uri.toURL();
    }

    /**
     * 创建临时 MultipartFile
     */
    private MultipartFile createTempFile(byte[] data, String originalUrl) {
        final String filename;
        if (originalUrl.contains(".")) {
            int lastDot = originalUrl.lastIndexOf('.');
            int lastSlash = originalUrl.lastIndexOf('/');
            if (lastDot > lastSlash) {
                filename = "image" + originalUrl.substring(lastDot);
            } else {
                filename = "temp_image";
            }
        } else {
            filename = "temp_image";
        }

        return new MultipartFile() {
            @Override public String getName() { return "file"; }
            @Override public String getOriginalFilename() { return filename; }
            @Override public String getContentType() { return "image/jpeg"; }
            @Override public boolean isEmpty() { return data == null || data.length == 0; }
            @Override public long getSize() { return data.length; }
            @Override public byte[] getBytes() { return data; }
            @Override public InputStream getInputStream() { return new ByteArrayInputStream(data); }
            @Override public void transferTo(File dest) {
                try { new FileOutputStream(dest).write(data); }
                catch (IOException e) { throw new RuntimeException(e); }
            }
        };
    }
}
