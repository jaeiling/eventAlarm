package com.example.eventalarm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class ImageStorageService {

    private final S3Client s3Client;
    private final String bucketName;
    private final String region;

    public ImageStorageService(
            @Value("${aws.s3.bucket-name}") String bucketName,
            @Value("${aws.s3.region}") String region,
            @Value("${aws.access-key}") String accessKey,
            @Value("${aws.secret-key}") String secretKey) {

        this.bucketName = bucketName;
        this.region = region;

        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    /**
     * S3에 이미지 업로드 후 퍼블릭 URL 반환
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf(".")).toLowerCase();
        }
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }

        String key = "eventalarm/" + UUID.randomUUID() + ext;
        String contentType = resolveContentType(ext);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }

        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    /**
     * S3 이미지 삭제 (URL에서 key 추출)
     */
    public void delete(String url) {
        if (url == null || url.isEmpty()) return;
        String key = extractKey(url);
        if (key == null) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
        } catch (Exception ignored) {}
    }

    // ── private ──────────────────────────────────────────────────

    private String extractKey(String url) {
        // https://{bucket}.s3.{region}.amazonaws.com/{key}
        String prefix = "amazonaws.com/";
        int idx = url.indexOf(prefix);
        if (idx == -1) return null;
        return url.substring(idx + prefix.length());
    }

    private String resolveContentType(String ext) {
        return switch (ext) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png"          -> "image/png";
            case ".gif"          -> "image/gif";
            case ".webp"         -> "image/webp";
            default              -> "application/octet-stream";
        };
    }
}
