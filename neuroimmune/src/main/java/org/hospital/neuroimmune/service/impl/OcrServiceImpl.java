package org.hospital.neuroimmune.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hospital.neuroimmune.config.OcrApiConfig;
import org.hospital.neuroimmune.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OCR服务实现 - 调用外部OCR API服务
 */
@Slf4j
@Service
public class OcrServiceImpl implements OcrService {

    @Autowired
    private OcrApiConfig ocrApiConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Map<String, Object> recognizeGeneralText(MultipartFile file) {
        return callOcrApi("/accurate", file);
    }

    @Override
    public Map<String, Object> recognizeMedicalReport(MultipartFile file) {
        Map<String, Object> result = callOcrApi("/accurate", file);
        result.put("type", "医疗报告");
        return result;
    }

    @Override
    public Map<String, Object> recognizeIdCard(MultipartFile file) {
        return callOcrApi("/idcard?side=front", file);
    }

    @Override
    public Map<String, Object> analyzeImageByUrl(String imageUrl) {
        try {
            // 从URL下载图片
            byte[] imageBytes = downloadImage(imageUrl);
            String filename = extractFilename(imageUrl);

            // 创建模拟的MultipartFile
            MockMultipartFile file = new MockMultipartFile(filename, imageBytes);

            return callOcrApi("/accurate", file);
        } catch (Exception e) {
            log.error("从URL识别图片失败: {}", e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("errorMsg", "识别失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> recognizeImagesByUrl(List<String> imageUrls) {
        Map<String, Object> result = new HashMap<>();
        StringBuilder allContent = new StringBuilder();
        boolean anySuccess = false;
        List<String> errors = new ArrayList<>();

        for (String imageUrl : imageUrls) {
            try {
                log.info("正在识别图片: {}", imageUrl);

                // 从URL下载图片
                byte[] imageBytes = downloadImage(imageUrl);
                String filename = extractFilename(imageUrl);

                // 创建模拟的MultipartFile
                MockMultipartFile file = new MockMultipartFile(filename, imageBytes);

                // 调用OCR API高精度通用文字接口
                Map<String, Object> ocrResult = callOcrApi("/accurate", file);

                if (ocrResult != null && Boolean.TRUE.equals(ocrResult.get("success"))) {
                    anySuccess = true;
                    String content = (String) ocrResult.get("fullText");
                    if (content != null && !content.isEmpty()) {
                        if (allContent.length() > 0) {
                            allContent.append("\n\n--- 第").append(errors.size() + 1).append("张图片 ---\n\n");
                        }
                        allContent.append(content);
                    }
                    log.info("图片识别成功: {}", filename);
                } else {
                    String errorMsg = ocrResult != null ? (String) ocrResult.get("errorMsg") : "未知错误";
                    errors.add(imageUrl + ": " + errorMsg);
                    log.warn("图片识别失败: {} - {}", filename, errorMsg);
                }
            } catch (Exception e) {
                errors.add(imageUrl + ": " + e.getMessage());
                log.error("识别图片异常: {} - {}", imageUrl, e.getMessage());
            }
        }

        result.put("success", anySuccess);
        result.put("content", allContent.toString());
        if (!errors.isEmpty()) {
            result.put("errors", errors);
        }

        return result;
    }

    /**
     * 调用外部OCR API
     */
    private Map<String, Object> callOcrApi(String apiPath, MultipartFile file) {
        try {
            String url = ocrApiConfig.getBaseUrl() + apiPath;
            log.info("调用OCR API: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> apiResponse = response.getBody();
                log.info("OCR API原始返回: {}", apiResponse);

                // 解析API返回结果
                // API返回格式: { code: 200, msg: "success", data: { success: true, fullText: "...", words: [...] } }
                Object codeObj = apiResponse.get("code");
                int code = 0;
                if (codeObj instanceof Integer) {
                    code = (Integer) codeObj;
                } else if (codeObj instanceof Number) {
                    code = ((Number) codeObj).intValue();
                }

                // code为200或1都算成功
                if (code == 200 || code == 1) {
                    Map<String, Object> data = (Map<String, Object>) apiResponse.get("data");
                    if (data != null) {
                        log.info("OCR识别成功");
                        return data;
                    }
                }

                // 返回错误信息
                log.warn("OCR API返回错误, code: {}, msg: {}", code, apiResponse.get("msg"));
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("success", false);
                errorResult.put("errorMsg", apiResponse.get("msg"));
                return errorResult;
            }

            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("errorMsg", "OCR API调用失败");
            return errorResult;

        } catch (Exception e) {
            log.error("调用OCR API失败: {}", e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("errorMsg", "OCR服务异常: " + e.getMessage());
            return errorResult;
        }
    }

    /**
     * 从URL下载图片
     */
    private byte[] downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        InputStream inputStream = url.openStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outputStream.close();
        return outputStream.toByteArray();
    }

    /**
     * 从URL提取文件名
     */
    private String extractFilename(String url) {
        int lastSlash = url.lastIndexOf('/');
        if (lastSlash >= 0) {
            return url.substring(lastSlash + 1);
        }
        return "image.jpg";
    }

    /**
     * 模拟MultipartFile实现
     */
    private static class MockMultipartFile implements MultipartFile {
        private final String filename;
        private final byte[] content;

        public MockMultipartFile(String filename, byte[] content) {
            this.filename = filename;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return filename;
        }

        @Override
        public String getContentType() {
            return "image/jpeg";
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() throws IOException {
            return content;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new java.io.ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            new java.io.FileOutputStream(dest).write(content);
        }
    }
}