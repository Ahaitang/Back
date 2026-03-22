package org.hospital.neuroimmune.service;

import io.minio.*;
import io.minio.http.Method;
import org.hospital.neuroimmune.config.MinioConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件存储服务
 */
@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 初始化 bucket，不存在则创建
     */
    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build());
            }
        } catch (Exception e) {
            System.err.println("MinIO bucket 初始化失败: " + e.getMessage());
        }
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

        // 返回文件访问路径
        return getFileUrl(objectName);
    }

    /**
     * 获取文件访问URL
     * @param objectName 对象名称
     * @return 访问URL
     */
    public String getFileUrl(String objectName) throws Exception {
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
        return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
    }

    /**
     * 删除文件
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(minioConfig.getBucketName())
                .object(objectName)
                .build());
    }
}