package com.example.eventalarm.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 학과 페이지 방문 기록
 * - 누가(IP), 언제, 어떤 페이지를, 어떤 기기로 봤는지 저장
 */
@Entity
@Table(name = "page_view")
public class PageView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 조회된 학과 페이지 ID */
    @Column(name = "department_page_id")
    private Long departmentPageId;

    /** 학교명 (집계용 편의 컬럼) */
    @Column(name = "university_name")
    private String universityName;

    /** 학과명 (집계용 편의 컬럼) */
    @Column(name = "department_name")
    private String departmentName;

    /** 방문자 IP (익명화 — 마지막 옥텟 제거, 예: 192.168.1.xxx) */
    @Column(name = "visitor_ip", length = 50)
    private String visitorIp;

    /** 기기 유형: MOBILE / TABLET / PC */
    @Column(name = "device_type", length = 20)
    private String deviceType;

    /** 방문 일시 */
    @Column(name = "viewed_at")
    private LocalDateTime viewedAt;

    public PageView() {}

    public PageView(Long departmentPageId, String universityName, String departmentName,
                    String visitorIp, String deviceType) {
        this.departmentPageId = departmentPageId;
        this.universityName = universityName;
        this.departmentName = departmentName;
        this.visitorIp = anonymize(visitorIp);
        this.deviceType = deviceType;
        this.viewedAt = LocalDateTime.now();
    }

    /** IP 마지막 옥텟 익명화 (192.168.1.100 → 192.168.1.xxx) */
    private String anonymize(String ip) {
        if (ip == null) return "unknown";
        int lastDot = ip.lastIndexOf('.');
        if (lastDot >= 0) return ip.substring(0, lastDot) + ".xxx";
        return ip;
    }

    // ── Getters ──────────────────────────────────────────────────

    public Long getId() { return id; }
    public Long getDepartmentPageId() { return departmentPageId; }
    public String getUniversityName() { return universityName; }
    public String getDepartmentName() { return departmentName; }
    public String getVisitorIp() { return visitorIp; }
    public String getDeviceType() { return deviceType; }
    public LocalDateTime getViewedAt() { return viewedAt; }
}
