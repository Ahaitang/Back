package org.hospital.neuroimmune.service.impl;

import org.hospital.neuroimmune.service.OcrService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * OCR服务实现 - 演示模式
 * 如需启用真实OCR功能，请添加阿里云SDK依赖并配置密钥
 */
@Service
public class OcrServiceImpl implements OcrService {

    @Override
    public Map<String, Object> recognizeGeneralText(MultipartFile file) {
        return createDemoResult("通用文字识别", file.getOriginalFilename());
    }

    @Override
    public Map<String, Object> recognizeMedicalReport(MultipartFile file) {
        Map<String, Object> result = createDemoResult("医疗报告识别", file.getOriginalFilename());
        result.put("type", "医疗报告");
        result.put("parsed", parseMedicalContent("演示内容"));
        return result;
    }

    @Override
    public Map<String, Object> recognizeIdCard(MultipartFile file) {
        Map<String, Object> result = createDemoResult("身份证识别", file.getOriginalFilename());
        Map<String, String> face = new HashMap<>();
        face.put("name", "演示用户");
        face.put("idNumber", "110101199001011234");
        result.put("face", face);
        return result;
    }

    @Override
    public Map<String, Object> analyzeImageByUrl(String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("demo", true);
        result.put("message", "演示模式：请配置阿里云OCR密钥");
        result.put("url", imageUrl);
        result.put("content", "【演示识别结果】\n图片URL: " + imageUrl);
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
        result.put("content", "【演示识别结果】\n这是一段演示文字识别结果。\n请添加阿里云OCR SDK依赖并配置密钥以启用真实识别功能。");
        return result;
    }

    /**
     * 解析医疗报告内容
     */
    private Map<String, Object> parseMedicalContent(String content) {
        Map<String, Object> parsed = new HashMap<>();
        parsed.put("diagnosis", "演示诊断结果");
        parsed.put("items", "演示检查项目");
        return parsed;
    }
}