package org.hospital.neuroimmune.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * 图像识别服务
 */
public interface OcrService {

    /**
     * 通用文字识别
     */
    Map<String, Object> recognizeGeneralText(MultipartFile file);

    /**
     * 医疗报告识别（检验报告、体检报告等）
     */
    Map<String, Object> recognizeMedicalReport(MultipartFile file);

    /**
     * 身份证识别
     */
    Map<String, Object> recognizeIdCard(MultipartFile file);

    /**
     * 通用图像分析（通过URL）
     */
    Map<String, Object> analyzeImageByUrl(String imageUrl);

    /**
     * 批量识别图片（通过URL列表）
     * 调用外部OCR API的高精度通用文字接口
     * @param imageUrls 图片URL列表
     * @return 合并后的识别结果
     */
    Map<String, Object> recognizeImagesByUrl(List<String> imageUrls);
}