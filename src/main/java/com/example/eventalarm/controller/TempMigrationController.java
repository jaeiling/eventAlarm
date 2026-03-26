package com.example.eventalarm.controller;

import com.example.eventalarm.repository.DepartmentPageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ⚠️ 임시 마이그레이션용 컨트롤러 - slug 업데이트 후 삭제할 것
 */
@RestController
public class TempMigrationController {

    private final DepartmentPageRepository repository;

    public TempMigrationController(DepartmentPageRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/admin/migrate-slugs")
    public ResponseEntity<String> migrateSlugs() {
        // 기존 페이지들에 slug 수동 설정
        repository.findById(1L).ifPresent(p -> {
            if (p.getSlug() == null || p.getSlug().isEmpty()) {
                p.setSlug(p.getUniversityName() + "-" + p.getDepartmentName());
                repository.save(p);
            }
        });
        repository.findById(2L).ifPresent(p -> {
            if (p.getSlug() == null || p.getSlug().isEmpty()) {
                p.setSlug(p.getUniversityName() + "-" + p.getDepartmentName());
                repository.save(p);
            }
        });
        repository.findById(3L).ifPresent(p -> {
            if (p.getSlug() == null || p.getSlug().isEmpty()) {
                p.setSlug(p.getUniversityName() + "-" + p.getDepartmentName());
                repository.save(p);
            }
        });
        repository.findById(4L).ifPresent(p -> {
            if (p.getSlug() == null || p.getSlug().isEmpty()) {
                p.setSlug(p.getUniversityName() + "-" + p.getDepartmentName());
                repository.save(p);
            }
        });

        // 결과 확인
        StringBuilder result = new StringBuilder("Migration complete!\n");
        repository.findAll().forEach(p ->
            result.append("id=").append(p.getId())
                  .append(", slug=").append(p.getSlug()).append("\n")
        );
        return ResponseEntity.ok(result.toString());
    }
}
