package org.hospital.ocr.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

/**
 * 文件工具类
 */
public class FileUtil {

    /**
     * 将MultipartFile转换为Base64字符串
     */
    public static String toBase64(MultipartFile file) throws Exception {
        byte[] bytes = file.getBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 获取文件扩展名
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex + 1) : "";
    }

    /**
     * 判断是否为图片文件
     */
    public static boolean isImage(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.startsWith("image/");
    }
}