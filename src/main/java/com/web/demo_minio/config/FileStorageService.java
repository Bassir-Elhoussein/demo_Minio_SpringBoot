package com.web.demo_minio.config;

/**
 * Author: Bassir El Houssein
 * Date: 6/26/2025
 */
import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.users}")
    private String userBucket;

    @PostConstruct
    public void initBucket() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(userBucket).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(userBucket).build());
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

    public String getUserImageUrl(String userId, String fileName) {
        return String.format("%s/%s/%s/%s", "http://localhost:9000", userBucket, userId, fileName);
    }
}
