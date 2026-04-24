package org.hospital.neuroimmune.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.hospital.neuroimmune.config.MinioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件存储服务
 */
@Slf4j
@Service
public class MinioService {

    @Autowired(required = false)
    private MinioClient minioClient;

    @Autowired(required = false)
    private MinioConfig minioConfig;

    /**
     * 检查 MinIO 是否可用
     */
    public boolean isAvailable() {
        return minioClient != null && minioConfig != null;
    }

    /**
     * 上传文件
     * @param file 文件
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file) throws Exception {
        return uploadFile(file, "");
    }

    /**
     * 上传文件到指定目录
     * @param file 文件
     * @param directory 目录路径
     * @return 文件访问路径
     */
    public String uploadFile(MultipartFile file, String directory) throws Exception {
        if (!isAvailable()) {
            throw new IllegalStateException("MinIO 服务未配置，请在 application.yaml 中配置 minio.access-key 和 minio.secret-key");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 按日期分目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName;
        if (directory != null && !directory.isEmpty()) {
            objectName = directory + "/" + dateDir + "/" + UUID.randomUUID().toString().replace("-", "") + extension;
        } else {
            objectName = dateDir + "/" + UUID.randomUUID().toString().replace("-", "") + extension;
        }

        // 上传文件
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build());

        // 返回永久访问路径（MinIO 已设置为公开读）
        return getPublicUrl(objectName);
    }

    /**
     * 获取文件访问URL
     * @param objectName 对象名称
     * @return 访问URL
     */
    public String getFileUrl(String objectName) throws Exception {
        if (!isAvailable()) {
            throw new IllegalStateException("MinIO 服务未配置");
        }
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(minioConfig.getBucketName())
                .object(objectName)
                .expiry(7, TimeUnit.DAYS)
                .build());
    }

    /**
     * 获取文件永久访问路径（公开读）
     * @param objectName 对象名称
     * @return 永久访问路径
     */
    public String getPublicUrl(String objectName) {
        if (!isAvailable()) {
            return null;
        }
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
    }

    /**
     * 删除文件
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) throws Exception {
        if (!isAvailable()) {
            throw new IllegalStateException("MinIO 服务未配置");
        }
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectName)
                .build());
    }
}