package com.example.eventalarm.service;

import com.example.eventalarm.domain.DepartmentPage;
import com.example.eventalarm.dto.DepartmentPageCreateDto;
import com.example.eventalarm.repository.DepartmentPageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional
public class DepartmentPageService {

    private final DepartmentPageRepository repository;

    public DepartmentPageService(DepartmentPageRepository repository) {
        this.repository = repository;
    }

    /**
     * 학과 페이지 생성 + 6자리 일련번호 발급
     * @return 생성된 DepartmentPage (serialNumber 포함)
     */
    public DepartmentPage create(DepartmentPageCreateDto dto) {
        DepartmentPage page = new DepartmentPage();
        page.setUniversityName(dto.getUniversityName());
        page.setDepartmentName(dto.getDepartmentName());
        page.setSerialNumber(generateUniqueSerialNumber());
        page.setSlug(generateUniqueSlug(dto.getUniversityName(), dto.getDepartmentName()));
        return repository.save(page);
    }

    /**
     * 일련번호 유효성 검증
     */
    @Transactional(readOnly = true)
    public boolean verifySerialNumber(Long pageId, String serialNumber) {
        return repository.findById(pageId)
                .map(p -> p.getSerialNumber().equals(serialNumber))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public DepartmentPage findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다. id=" + id));
    }

    /** 전체 학과 페이지 목록 (생성 최신순) */
    @Transactional(readOnly = true)
    public java.util.List<DepartmentPage> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    /** slug로 페이지 조회 */
    @Transactional(readOnly = true)
    public java.util.Optional<DepartmentPage> findBySlug(String slug) {
        return repository.findBySlug(slug);
    }

    // ── private ──────────────────────────────────────────────────

    /** 중복 없는 6자리 랜덤 숫자 문자열 생성 */
    private String generateUniqueSerialNumber() {
        Random random = new Random();
        String serial;
        int maxTry = 100;
        do {
            // 100000 ~ 999999
            int num = 100000 + random.nextInt(900000);
            serial = String.valueOf(num);
            maxTry--;
            if (maxTry <= 0) throw new IllegalStateException("일련번호 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
        } while (repository.existsBySerialNumber(serial));
        return serial;
    }

    /** 대학교명-학과명 기반 고유 slug 생성 */
    private String generateUniqueSlug(String universityName, String departmentName) {
        // 공백 → 하이픈, 특수문자 제거
        String base = (universityName + "-" + departmentName)
                .replaceAll("\\s+", "-")
                .replaceAll("[^가-힣a-zA-Z0-9\\-]", "");

        if (!repository.existsBySlug(base)) return base;

        // 중복 시 뒤에 숫자 붙이기
        int suffix = 2;
        while (repository.existsBySlug(base + "-" + suffix)) suffix++;
        return base + "-" + suffix;
    }
}
