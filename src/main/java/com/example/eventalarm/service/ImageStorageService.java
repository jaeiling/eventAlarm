package com.example.eventalarm.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageStorageService {

    private final Cloudinary cloudinary;

    public ImageStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret,
                "secure",     true
        ));
    }

    /**
     * Cloudinary에 이미지 업로드 후 URL 반환
     * storedFileName 대신 Cloudinary URL을 저장
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

        try {
            Map result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "eventalarm",
                            "resource_type", "image"
                    ));
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
        }
    }

    /**
     * Cloudinary 이미지 삭제
     * URL에서 public_id 추출 후 삭제
     */
    public void delete(String storedUrl) {
        if (storedUrl == null || storedUrl.isEmpty()) return;
        try {
            // URL에서 public_id 추출: .../eventalarm/filename.jpg → eventalarm/filename
            String publicId = extractPublicId(storedUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (IOException ignored) {}
    }

    private String extractPublicId(String url) {
        try {
            // https://res.cloudinary.com/{cloud}/image/upload/v123/{folder}/{name}.{ext}
            int uploadIdx = url.indexOf("/upload/");
            if (uploadIdx == -1) return null;
            String afterUpload = url.substring(uploadIdx + 8);
            // v123/ 버전 prefix 제거
            if (afterUpload.matches("v\\d+/.*")) {
                afterUpload = afterUpload.replaceFirst("v\\d+/", "");
            }
            // 확장자 제거
            int dotIdx = afterUpload.lastIndexOf('.');
            if (dotIdx != -1) afterUpload = afterUpload.substring(0, dotIdx);
            return afterUpload;
        } catch (Exception e) {
            return null;
        }
    }
}
