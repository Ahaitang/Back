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
}