package com.example.eventalarm.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 행사 엔티티
 */
@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_page_id", nullable = false)
    private DepartmentPage departmentPage;

    @Column(nullable = false)
    private String title;           // 행사명

    @Column(nullable = false)
    private LocalDateTime eventDateTime;  // 행사 시작 일시

    @Column
    private LocalDateTime eventEndDateTime; // 행사 종료 일시 (선택, null이면 당일)

    @Column(nullable = false)
    private String location;        // 장소명 (예: 인천대 7호관)

    @Column
    private String locationAddress; // 검색용 주소 (지도 링크에 사용)

    @Column(columnDefinition = "TEXT")
    private String description;     // 행사 내용/설명

    @Column
    private String fee;             // 회비 안내 (예: 20,000원)

    @Column
    private String bankAccount;     // 계좌번호 (예: 카카오뱅크 3333-00-0000000 홍길동)

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sortOrder ASC")
    private List<EventImage> images = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() { return id; }

    public DepartmentPage getDepartmentPage() { return departmentPage; }
    public void setDepartmentPage(DepartmentPage departmentPage) { this.departmentPage = departmentPage; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDateTime getEventDateTime() { return eventDateTime; }
    public void setEventDateTime(LocalDateTime eventDateTime) { this.eventDateTime = eventDateTime; }

    public LocalDateTime getEventEndDateTime() { return eventEndDateTime; }
    public void setEventEndDateTime(LocalDateTime eventEndDateTime) { this.eventEndDateTime = eventEndDateTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFee() { return fee; }
    public void setFee(String fee) { this.fee = fee; }

    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }

    public List<EventImage> getImages() { return images; }
    public void setImages(List<EventImage> images) { this.images = images; }

    /** 대표 이미지 반환 (없으면 첫 번째, 그것도 없으면 null) */
    public EventImage getThumbnailImage() {
        return images.stream().filter(EventImage::isThumbnail).findFirst()
                .orElse(images.isEmpty() ? null : images.get(0));
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
