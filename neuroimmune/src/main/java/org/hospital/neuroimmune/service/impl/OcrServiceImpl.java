package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.service.OcrService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.aliyun.ocr_api20200507.Client;
import com.aliyun.ocr_api20200507.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 阿里云OCR服务实现
 */
@Service
public class OcrServiceImpl implements OcrService {

    @Value("${aliyun.ocr.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.ocr.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.ocr.endpoint:ocr-api.cn-hangzhou.aliyuncs.com}")
    private String endpoint;

    private Client client;

    @PostConstruct
    public void init() throws Exception {
        if (accessKeyId != null && !accessKeyId.isEmpty() && !"your-access-key-id".equals(accessKeyId)) {
            Config config = new Config()
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint(endpoint);
            this.client = new Client(config);
        }
    }

    @Override
    public Map<String, Object> recognizeGeneralText(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (client == null) {
                return createDemoResult("通用文字识别", file.getOriginalFilename());
            }

            RecognizeGeneralRequest request = new RecognizeGeneralRequest()
                    .setBody(file.getInputStream());
            RuntimeOptions runtime = new RuntimeOptions();
            RecognizeGeneralResponse response = client.recognizeGeneralWithOptions(request, runtime);

            if (response.getBody() != null && response.getBody().getData() != null) {
                result.put("success", true);
                result.put("content", response.getBody().getData().getContent());
                result.put("blocks", response.getBody().getData().getBlock());
            } else {
                result.put("success", false);
                result.put("message", "识别失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "识别异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> recognizeMedicalReport(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (client == null) {
                return createDemoResult("医疗报告识别", file.getOriginalFilename());
            }

            // 使用通用文字识别处理医疗报告
            RecognizeGeneralRequest request = new RecognizeGeneralRequest()
                    .setBody(file.getInputStream());
            RuntimeOptions runtime = new RuntimeOptions();
            RecognizeGeneralResponse response = client.recognizeGeneralWithOptions(request, runtime);

            if (response.getBody() != null && response.getBody().getData() != null) {
                String content = response.getBody().getData().getContent();
                result.put("success", true);
                result.put("content", content);
                result.put("type", "医疗报告");
                // 可以进一步解析报告内容
                result.put("parsed", parseMedicalContent(content));
            } else {
                result.put("success", false);
                result.put("message", "识别失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "识别异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> recognizeIdCard(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (client == null) {
                return createDemoResult("身份证识别", file.getOriginalFilename());
            }

            RecognizeIdcardRequest request = new RecognizeIdcardRequest()
                    .setBody(file.getInputStream());
            RuntimeOptions runtime = new RuntimeOptions();
            RecognizeIdcardResponse response = client.recognizeIdcardWithOptions(request, runtime);

            if (response.getBody() != null && response.getBody().getData() != null) {
                result.put("success", true);
                result.put("face", response.getBody().getData().getFace());
            } else {
                result.put("success", false);
                result.put("message", "识别失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "识别异常: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> analyzeImageByUrl(String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (client == null) {
                result.put("success", true);
                result.put("content", "演示模式：图片URL识别结果");
                result.put("url", imageUrl);
                return result;
            }

            RecognizeGeneralRequest request = new RecognizeGeneralRequest()
                    .setUrl(imageUrl);
            RuntimeOptions runtime = new RuntimeOptions();
            RecognizeGeneralResponse response = client.recognizeGeneralWithOptions(request, runtime);

            if (response.getBody() != null && response.getBody().getData() != null) {
                result.put("success", true);
                result.put("content", response.getBody().getData().getContent());
            } else {
                result.put("success", false);
                result.put("message", "识别失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "识别异常: " + e.getMessage());
        }
        return result;
    }

    /**
     * 创建演示结果（未配置密钥时使用）
     */
    private Map<String, Object> createDemoResult(String type, String filename) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("demo", true);
        result.put("message", "演示模式：请配置阿里云OCR密钥");
        result.put("type", type);
        result.put("filename", filename);
        result.put("content", "【演示识别结果】\n这是一段演示文字识别结果。\n请在 application.properties 中配置阿里云OCR密钥以启用真实识别功能。");
        return result;
    }

    /**
     * 解析医疗报告内容
     */
    private Map<String, Object> parseMedicalContent(String content) {
        Map<String, Object> parsed = new HashMap<>();
        if (content == null) {
            return parsed;
        }

        // 简单的关键词提取
        String[] lines = content.split("\n");
        StringBuilder diagnosis = new StringBuilder();
        StringBuilder items = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.contains("诊断") || line.contains("结果")) {
                diagnosis.append(line).append("; ");
            }
            if (line.contains("指标") || line.contains("检验") || line.contains("检查")) {
                items.append(line).append("; ");
            }
        }

        parsed.put("diagnosis", diagnosis.toString());
        parsed.put("items", items.toString());
        return parsed;
    }
}