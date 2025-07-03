package com.web.demo_minio.config;

/**
 * Author: Bassir El Houssein
 * Date: 6/26/2025
 */

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.users}")
    private String userBucket;

    @PostConstruct
    public void initBucket() throws Exception {
        boolean found = minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(userBucket).build());
        if (!found) {
            minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(userBucket).build());
        }
    }

    public void uploadUserProfilePicture(String userId, MultipartFile file) throws Exception {
        String objectName = userId + "/" + file.getOriginalFilename();
        InputStream is = file.getInputStream();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(userBucket)
                        .object(objectName)
                        .stream(is, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );
    }

    public String getUserImageUrl(String userId, String fileName) throws Exception {
        String objectName = userId + "/" + fileName;

        return minioClient.getPresignedObjectUrl(
                io.minio.GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(userBucket)
                        .object(objectName)
                        .extraQueryParams(Map.of("response-content-type", getContentType(fileName)))
                        .build()
        );
    }

    // New method for proxy: returns InputStream of object
    public InputStream getUserImageStream(String userId, String fileName) throws Exception {
        String objectName = userId + "/" + fileName;

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(userBucket)
                        .object(objectName)
                        .build()
        );
    }

    // Make public for controller
    public String getContentType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".webp")) return "image/webp";
        return "application/octet-stream";
    }
}
