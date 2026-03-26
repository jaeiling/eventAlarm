package com.example.eventalarm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${app.upload.dir:uploads/events}")
    private String uploadDir;

    /**
     * 파일 저장 후 저장된 파일명(UUID) 반환
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf(".")).toLowerCase();
        }
        // 허용 확장자 체크
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다.");
        }

        String stored = UUID.randomUUID().toString() + ext;
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
        return stored;
    }

    /**
     * 파일 삭제
     */
    public void delete(String storedFileName) {
        if (storedFileName == null) return;
        try {
            Files.deleteIfExists(Paths.get(uploadDir, storedFileName));
        } catch (IOException ignored) {}
    }
}
