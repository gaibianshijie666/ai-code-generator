package com.chen.yuaicodemother.utils;

import io.minio.*;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
public class MinioUtils {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    public MinioUtils(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 上传本地文件（通过 File 对象）
     */
    public String uploadFile(File file) {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在: " + file);
        }

        String filename = UUID.randomUUID() + "_" + file.getName();
        String contentType = guessContentType(file.getName());

        try {
            // 检查桶是否存在
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            // 上传文件流
            try (InputStream in = new FileInputStream(file)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(filename)
                                .stream(in, file.length(), -1)
                                .contentType(contentType)
                                .build()
                );
            }

            // 返回访问 URL
            return String.format("%s/%s/%s", endpoint, bucket, filename);

        } catch (MinioException e) {
            log.error("上传到 MinIO 失败: {}", e.getMessage());
            throw new RuntimeException("上传失败");
        } catch (Exception e) {
            log.error("上传失败", e);
            throw new RuntimeException("上传失败", e);
        }
    }

    /**
     * 上传本地文件（通过文件路径）
     */
    public String uploadFile(String filePath) {
        return uploadFile(new File(filePath));
    }

    /**
     * 简单的 Content-Type 判断
     */
    private String guessContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream"; // 默认类型
    }
}
