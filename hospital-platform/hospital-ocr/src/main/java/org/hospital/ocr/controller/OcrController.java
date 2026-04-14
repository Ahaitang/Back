package org.hospital.ocr.controller;

import lombok.extern.slf4j.Slf4j;
import org.hospital.common.result.Result;
import org.hospital.ocr.dto.OcrResult;
import org.hospital.ocr.service.BaiduOcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * OCR识别接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ocr")
@CrossOrigin
public class OcrController {

    @Autowired
    private BaiduOcrService ocrService;

    /**
     * 通用文字识别
     */
    @PostMapping("/general")
    public Result<OcrResult> generalBasic(@RequestParam("file") MultipartFile file) {
        try {
            OcrResult result = ocrService.generalBasic(file);
            return Result.success(result);
        } catch (IOException e) {
            log.error("通用文字识别失败", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 通用文字识别（高精度版）
     */
    @PostMapping("/accurate")
    public Result<OcrResult> accurateBasic(@RequestParam("file") MultipartFile file) {
        try {
            OcrResult result = ocrService.accurateBasic(file);
            return Result.success(result);
        } catch (IOException e) {
            log.error("高精度文字识别失败", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * PaddleOCR-VL通用识别（免费API）
     * 可自定义prompt指导识别
     */
    @PostMapping("/paddle")
    public Result<OcrResult> paddleOcr(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "prompt", required = false) String prompt) {
        try {
            OcrResult result = ocrService.paddleOcrVl(file, prompt);
            return Result.success(result);
        } catch (Exception e) {
            log.error("PaddleOCR-VL识别失败", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 身份证识别
     * @param file 图片文件
     * @param side front-正面(有照片) back-反面(有国徽)
     */
    @PostMapping("/idcard")
    public Result<OcrResult> idCard(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "side", defaultValue = "front") String side) {
        try {
            OcrResult result = ocrService.idCard(file, side);
            return Result.success(result);
        } catch (IOException e) {
            log.error("身份证识别失败", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 银行卡识别
     */
    @PostMapping("/bankcard")
    public Result<OcrResult> bankCard(@RequestParam("file") MultipartFile file) {
        try {
            OcrResult result = ocrService.bankCard(file);
            return Result.success(result);
        } catch (IOException e) {
            log.error("银行卡识别失败", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 驾驶证识别
     */
    @PostMapping("/driving")
    public Result<OcrResult> drivingLicense(@RequestParam("file") MultipartFile file) {
        try {
            OcrResult result = ocrService.drivingLicense(file);
            return Result.success(result);
        } catch (IOException e) {
            log.error("驾驶证识别失败", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 行驶证识别
     */
    @PostMapping("/vehicle")
    public Result<OcrResult> vehicleLicense(@RequestParam("file") MultipartFile file) {
        try {
            OcrResult result = ocrService.vehicleLicense(file);
            return Result.success(result);
        } catch (IOException e) {
            log.error("行驶证识别失败", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 医疗发票识别
     */
    @PostMapping("/medical-invoice")
    public Result<OcrResult> medicalInvoice(@RequestParam("file") MultipartFile file) {
        try {
            OcrResult result = ocrService.medicalInvoice(file);
            return Result.success(result);
        } catch (IOException e) {
            log.error("医疗发票识别失败", e);
            return Result.error("识别失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OCR服务正常运行");
    }
}