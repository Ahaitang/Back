package org.hospital.neuroimmune.controller;

import org.hospital.neuroimmune.common.Result;
import org.hospital.neuroimmune.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 图像识别控制器
 */
@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    /**
     * 通用文字识别
     */
    @PostMapping("/general")
    public Result<Map<String, Object>> recognizeGeneral(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = ocrService.recognizeGeneralText(file);
        return Result.success(result);
    }

    /**
     * 医疗报告识别
     */
    @PostMapping("/medical-report")
    public Result<Map<String, Object>> recognizeMedicalReport(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = ocrService.recognizeMedicalReport(file);
        return Result.success(result);
    }

    /**
     * 身份证识别
     */
    @PostMapping("/idcard")
    public Result<Map<String, Object>> recognizeIdCard(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = ocrService.recognizeIdCard(file);
        return Result.success(result);
    }

    /**
     * 通过URL识别图片
     */
    @PostMapping("/url")
    public Result<Map<String, Object>> recognizeByUrl(@RequestParam("url") String url) {
        Map<String, Object> result = ocrService.analyzeImageByUrl(url);
        return Result.success(result);
    }

    /**
     * 解析外院医疗资料（Mock版本）
     * 上传图片后返回示例解析内容
     */
    @PostMapping("/parse-medical")
    public Result<Map<String, Object>> parseMedicalRecord(@RequestBody Map<String, Object> request) {
        // Mock 返回示例数据
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("content", "【外院就诊资料解析结果】\n\n" +
                "就诊日期：2024年3月15日\n" +
                "主诉：头痛伴视物模糊3天\n" +
                "现病史：患者3天前无明显诱因出现头痛，呈持续性胀痛，伴视物模糊，无恶心呕吐，无肢体无力。\n" +
                "诊断：偏头痛\n" +
                "建议：注意休息，避免过度劳累，定期复查。\n\n" +
                "（注：此为演示数据，请根据实际情况修改）");
        return Result.success(result);
    }
}