package org.hospital.common.ai;

import lombok.extern.slf4j.Slf4j;
import org.hospital.ocr.service.BaiduOcrService;
import org.hospital.ocr.dto.OcrResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.ResponseEntity;
import java.util.*;
import java.util.Base64;

/**
 * AI Service Client - Python first, Java fallback
 */
@Slf4j
@Component
public class AIServiceClient {

    @Value("${ai-service.url:http://localhost:8000}")
    private String aiServiceUrl;

    @Autowired(required = false)
    private BaiduOcrService baiduOcrService;

    private final RestTemplate restTemplate;

    public AIServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get QMG trend analysis
     */
    public Map<String, Object> getQmgTrend(Integer patientId, String startDate, String endDate) {
        String url = aiServiceUrl + "/api/qmg/trend/" + patientId;
        if (startDate != null) url += "?start_date=" + startDate;
        if (endDate != null) url += (startDate != null ? "&" : "?") + "end_date=" + endDate;
        return restTemplate.getForEntity(url, Map.class).getBody();
    }

    /**
     * Get anomaly detection
     */
    public Map<String, Object> detectAnomaly(Integer patientId, Integer recentRecords) {
        String url = aiServiceUrl + "/api/qmg/anomaly/" + patientId + "?recent_records=" + recentRecords;
        return restTemplate.getForEntity(url, Map.class).getBody();
    }

    /**
     * Get followup priority
     */
    public Map<String, Object> getFollowupPriority(Integer doctorId) {
        String url = aiServiceUrl + "/api/neuro/followup-priority";
        if (doctorId != null) url += "?doctor_id=" + doctorId;
        return restTemplate.getForEntity(url, Map.class).getBody();
    }

    /**
     * Get optimal followup time
     */
    public Map<String, Object> getOptimalTime(Integer patientId) {
        return restTemplate.getForEntity(
            aiServiceUrl + "/api/neuro/optimal-time/" + patientId, Map.class
        ).getBody();
    }

    /**
     * Check overdue followups
     */
    public Map<String, Object> checkOverdue(Integer doctorId) {
        String url = aiServiceUrl + "/api/neuro/overdue-check";
        if (doctorId != null) url += "?doctor_id=" + doctorId;
        return restTemplate.getForEntity(url, Map.class).getBody();
    }

    /**
     * OCR parse - Python PaddleOCR first, Baidu OCR fallback
     */
    public Map<String, Object> parseMedicalRecord(List<String> imageBase64s) {
        // 1. Try Python AI service first
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("images", imageBase64s);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                aiServiceUrl + "/api/ocr/parse", body, Map.class
            );

            Map<String, Object> result = response.getBody();
            if (result != null && Boolean.TRUE.equals(((Map)result.get("data")).get("success"))) {
                log.info("OCR success from Python PaddleOCR");
                return result;
            }

            log.warn("Python OCR returned failure, trying Baidu fallback");
        } catch (Exception e) {
            log.warn("Python OCR service unavailable: {}, trying Baidu fallback", e.getMessage());
        }

        // 2. Fallback to Java Baidu OCR
        if (baiduOcrService != null) {
            try {
                // Convert first image from base64
                byte[] imageBytes = Base64.getDecoder().decode(imageBase64s.get(0));

                // Create mock MultipartFile or use byte array directly
                OcrResult ocrResult = baiduOcrService.generalBasic(createMultipartFile(imageBytes));

                if (ocrResult.isSuccess()) {
                    log.info("OCR success from Baidu fallback");
                    Map<String, Object> result = new HashMap<>();
                    result.put("code", 200);
                    result.put("message", "success (baidu fallback)");
                    result.put("data", new HashMap<String, Object>());
                    ((Map)result.get("data")).put("success", true);
                    ((Map)result.get("data")).put("full_text", ocrResult.getFullText());
                    ((Map)result.get("data")).put("source", "baidu");
                    return result;
                }
            } catch (Exception e) {
                log.error("Baidu OCR fallback also failed: {}", e.getMessage());
            }
        }

        // 3. Both failed
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", "OCR识别失败");
        result.put("data", new HashMap<String, Object>());
        ((Map)result.get("data")).put("success", false);
        ((Map)result.get("data")).put("error", "Python和百度OCR均失败");
        return result;
    }

    /**
     * OCR parse with files - Python first, Baidu fallback
     */
    public Map<String, Object> parseMedicalFiles(List<MultipartFile> files) {
        // 1. Try Python first
        try {
            List<String> base64s = new ArrayList<>();
            for (MultipartFile f : files) {
                base64s.add(Base64.getEncoder().encodeToString(f.getBytes()));
            }
            return parseMedicalRecord(base64s);
        } catch (Exception e) {
            log.error("Failed to encode files: {}", e.getMessage());
        }

        // 2. Fallback to Baidu directly
        if (baiduOcrService != null && !files.isEmpty()) {
            try {
                OcrResult ocrResult = baiduOcrService.generalBasic(files.get(0));
                Map<String, Object> result = new HashMap<>();
                result.put("code", ocrResult.isSuccess() ? 200 : 500);
                result.put("message", ocrResult.isSuccess() ? "success (baidu)" : ocrResult.getErrorMsg());
                result.put("data", new HashMap<String, Object>());
                if (ocrResult.isSuccess()) {
                    ((Map)result.get("data")).put("full_text", ocrResult.getFullText());
                    ((Map)result.get("data")).put("source", "baidu");
                }
                return result;
            } catch (Exception e) {
                log.error("Baidu OCR failed: {}", e.getMessage());
            }
        }

        return Map.of("code", 500, "message", "OCR识别失败");
    }

    private MultipartFile createMultipartFile(byte[] bytes) {
        return new MockMultipartFile(bytes);
    }

    /**
     * Simple Mock MultipartFile implementation
     */
    private static class MockMultipartFile implements MultipartFile {
        private final byte[] bytes;

        MockMultipartFile(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override public String getName() { return "image"; }
        @Override public String getOriginalFilename() { return "image.jpg"; }
        @Override public String getContentType() { return "image/jpeg"; }
        @Override public boolean isEmpty() { return bytes == null || bytes.length == 0; }
        @Override public long getSize() { return bytes.length; }
        @Override public byte[] getBytes() { return bytes; }
        @Override public java.io.InputStream getInputStream() { return new java.io.ByteArrayInputStream(bytes); }
        @Override public void transferTo(java.io.File dest) throws java.io.IOException {
            new java.io.FileOutputStream(dest).write(bytes);
        }
    }
}