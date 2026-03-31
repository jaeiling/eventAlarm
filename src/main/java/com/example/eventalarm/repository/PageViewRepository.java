package com.example.eventalarm.repository;

import com.example.eventalarm.domain.PageView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PageViewRepository extends JpaRepository<PageView, Long> {

    /** 전체 조회수 */
    long count();

    /** 오늘 조회수 */
    @Query("SELECT COUNT(v) FROM PageView v WHERE v.viewedAt >= :startOfDay")
    long countTodayViews(@Param("startOfDay") LocalDateTime startOfDay);

    /** 오늘 순 방문자 수 (IP 기준 중복 제거) */
    @Query("SELECT COUNT(DISTINCT v.visitorIp) FROM PageView v WHERE v.viewedAt >= :startOfDay")
    long countTodayUniqueVisitors(@Param("startOfDay") LocalDateTime startOfDay);

    /** 최근 5분 접속자 수 (실시간 접속자) */
    @Query("SELECT COUNT(DISTINCT v.visitorIp) FROM PageView v WHERE v.viewedAt >= :fiveMinAgo")
    long countRecentVisitors(@Param("fiveMinAgo") LocalDateTime fiveMinAgo);

    /** 학과 페이지별 조회수 TOP 10 */
    @Query("SELECT v.universityName, v.departmentName, COUNT(v) as cnt " +
           "FROM PageView v GROUP BY v.universityName, v.departmentName " +
           "ORDER BY cnt DESC")
    List<Object[]> findTopPages();

    /** 기기 유형별 비율 */
    @Query("SELECT v.deviceType, COUNT(v) FROM PageView v GROUP BY v.deviceType")
    List<Object[]> countByDeviceType();

    /** 일별 방문자 추이 (최근 7일) */
    @Query("SELECT CAST(v.viewedAt AS date), COUNT(DISTINCT v.visitorIp) " +
           "FROM PageView v WHERE v.viewedAt >= :since " +
           "GROUP BY CAST(v.viewedAt AS date) ORDER BY CAST(v.viewedAt AS date)")
    List<Object[]> findDailyVisitors(@Param("since") LocalDateTime since);

    /** 최근 방문 기록 50개 (시간 역순) */
    List<PageView> findTop50ByOrderByViewedAtDesc();
}
