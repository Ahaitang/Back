package org.hospital.ocr.dto;

import lombok.Data;

import java.util.List;

/**
 * OCR识别结果
 */
@Data
public class OcrResult {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 识别出的文字列表
     */
    private List<String> words;

    /**
     * 完整文字（换行连接）
     */
    private String fullText;

    /**
     * 结构化数据（身份证、银行卡等）
     */
    private Object data;
}