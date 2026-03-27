package com.example.eventalarm.repository;

import com.example.eventalarm.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // ── 공지글 ────────────────────────────────────────────────────

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.images " +
           "WHERE e.departmentPage.id = :pageId AND e.postType = 'NOTICE' ORDER BY e.createdAt DESC")
    List<Event> findNoticesByDepartmentPageIdOrderByCreatedAtDesc(@Param("pageId") Long pageId);

    // ── 행사 (postType = 'EVENT' 또는 null인 기존 데이터 포함) ──

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.images " +
           "WHERE e.departmentPage.id = :pageId AND (e.postType = 'EVENT' OR e.postType IS NULL) " +
           "ORDER BY e.eventDateTime ASC")
    List<Event> findEventsByDepartmentPageIdOrderByEventDateTimeAsc(@Param("pageId") Long pageId);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.images " +
           "WHERE e.departmentPage.id = :pageId AND (e.postType = 'EVENT' OR e.postType IS NULL) " +
           "AND e.eventDateTime > :now ORDER BY e.eventDateTime ASC")
    List<Event> findEventsByDepartmentPageIdAndEventDateTimeAfterOrderByEventDateTimeAsc(
            @Param("pageId") Long pageId, @Param("now") LocalDateTime now);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.images " +
           "WHERE e.departmentPage.id = :pageId AND (e.postType = 'EVENT' OR e.postType IS NULL) " +
           "AND e.eventDateTime > :now ORDER BY e.eventDateTime DESC")
    List<Event> findEventsByDepartmentPageIdAndEventDateTimeAfterOrderByEventDateTimeDesc(
            @Param("pageId") Long pageId, @Param("now") LocalDateTime now);
}
