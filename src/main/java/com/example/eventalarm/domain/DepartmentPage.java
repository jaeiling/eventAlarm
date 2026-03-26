package com.example.eventalarm.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 학과 페이지 엔티티
 * 예: 인천대 정보통신공학과
 */
@Entity
@Table(name = "department_page")
public class DepartmentPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String universityName;   // 대학교명 (예: 인천대학교)

    @Column(nullable = false)
    private String departmentName;   // 학과명 (예: 정보통신공학과)

    /** 관리자 일련번호 (6자리 랜덤 정수, unique) - 수정/삭제 권한 키 */
    @Column(nullable = false, unique = true, length = 6)
    private String serialNumber;

    @Column(unique = true)
    private String slug; // URL 슬러그: 인천대학교-정보통신공학과

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() { return id; }

    public String getUniversityName() { return universityName; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
