package com.example.eventalarm.service;

import com.example.eventalarm.domain.PageView;
import com.example.eventalarm.repository.PageViewRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatsService {

    private final PageViewRepository pageViewRepository;

    public StatsService(PageViewRepository pageViewRepository) {
        this.pageViewRepository = pageViewRepository;
    }

    /**
     * 학과 페이지 조회 시 방문 기록 저장
     */
    @Transactional
    public void record(Long pageId, String universityName, String departmentName,
                       HttpServletRequest request) {
        String ip = extractIp(request);
        String device = detectDevice(request.getHeader("User-Agent"));
        pageViewRepository.save(new PageView(pageId, universityName, departmentName, ip, device));
    }

    /**
     * 대시보드용 통계 데이터 반환
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStats(int logPage) {
        Map<String, Object> stats = new LinkedHashMap<>();

        LocalDateTime startOfDay  = LocalDate.now().atStartOfDay();
        LocalDateTime fiveMinAgo  = LocalDateTime.now().minusMinutes(5);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // ── 기본 수치 ──────────────────────────────────────────
        stats.put("totalViews",       pageViewRepository.count());
        stats.put("todayViews",       pageViewRepository.countTodayViews(startOfDay));
        stats.put("todayUnique",      pageViewRepository.countTodayUniqueVisitors(startOfDay));
        stats.put("realtimeVisitors", pageViewRepository.countRecentVisitors(fiveMinAgo));

        // ── 학과 페이지 TOP 10 ─────────────────────────────────
        List<Map<String, Object>> topPages = new ArrayList<>();
        for (Object[] row : pageViewRepository.findTopPages()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("universityName",  row[0]);
            item.put("departmentName",  row[1]);
            item.put("views",           row[2]);
            topPages.add(item);
            if (topPages.size() >= 10) break;
        }
        stats.put("topPages", topPages);

        // ── 기기 유형 비율 ─────────────────────────────────────
        Map<String, Long> deviceStats = new LinkedHashMap<>();
        deviceStats.put("MOBILE",  0L);
        deviceStats.put("TABLET",  0L);
        deviceStats.put("PC",      0L);
        for (Object[] row : pageViewRepository.countByDeviceType()) {
            deviceStats.put((String) row[0], (Long) row[1]);
        }
        stats.put("deviceStats", deviceStats);

        // ── 일별 방문자 추이 (최근 7일) ────────────────────────
        List<Map<String, Object>> dailyVisitors = new ArrayList<>();
        long maxDailyVisitors = 1L;
        for (Object[] row : pageViewRepository.findDailyVisitors(sevenDaysAgo)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date",     row[0].toString());
            item.put("visitors", row[1]);
            dailyVisitors.add(item);
            long v = ((Number) row[1]).longValue();
            if (v > maxDailyVisitors) maxDailyVisitors = v;
        }
        stats.put("dailyVisitors", dailyVisitors);
        stats.put("maxDailyVisitors", maxDailyVisitors);

        // ── 방문 로그 (페이지네이션, 50개씩) ───────────────────
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM.dd HH:mm:ss");
        Page<PageView> logPageResult = pageViewRepository.findAllByOrderByViewedAtDesc(
                PageRequest.of(logPage, 50));
        List<Map<String, Object>> recentLogs = new ArrayList<>();
        for (PageView v : logPageResult.getContent()) {
            Map<String, Object> log = new LinkedHashMap<>();
            log.put("time", v.getViewedAt().format(fmt));
            log.put("page", v.getUniversityName() + " " + v.getDepartmentName());
            log.put("device", v.getDeviceType());
            log.put("ip", v.getVisitorIp());
            recentLogs.add(log);
        }
        stats.put("recentLogs", recentLogs);
        stats.put("logPage", logPage);
        stats.put("logTotalPages", logPageResult.getTotalPages());
        stats.put("logHasPrev", logPage > 0);
        stats.put("logHasNext", logPage < logPageResult.getTotalPages() - 1);

        return stats;
    }

    // ── private 유틸 ──────────────────────────────────────────────

    /** 프록시 환경 포함 IP 추출 */
    private String extractIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) return ip.split(",")[0].trim();
        return request.getRemoteAddr();
    }

    /** User-Agent로 기기 유형 판별 */
    private String detectDevice(String ua) {
        if (ua == null) return "PC";
        String lower = ua.toLowerCase();
        if (lower.contains("tablet") || lower.contains("ipad")) return "TABLET";
        if (lower.contains("mobile") || lower.contains("android") ||
            lower.contains("iphone") || lower.contains("ipod")) return "MOBILE";
        return "PC";
    }
}
