package com.example.eventalarm.repository;

import com.example.eventalarm.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 해당 학과 페이지의 모든 행사 + 이미지 한 번에 (N+1 방지)
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.images " +
           "WHERE e.departmentPage.id = :pageId ORDER BY e.eventDateTime ASC")
    List<Event> findByDepartmentPageIdOrderByEventDateTimeAsc(@Param("pageId") Long pageId);

    // 미래 행사만 + 이미지 한 번에, 날짜 오름차순
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.images " +
           "WHERE e.departmentPage.id = :pageId AND e.eventDateTime > :now ORDER BY e.eventDateTime ASC")
    List<Event> findByDepartmentPageIdAndEventDateTimeAfterOrderByEventDateTimeAsc(
            @Param("pageId") Long pageId, @Param("now") LocalDateTime now);

    // 미래 행사만 + 이미지 한 번에, 날짜 내림차순
    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.images " +
           "WHERE e.departmentPage.id = :pageId AND e.eventDateTime > :now ORDER BY e.eventDateTime DESC")
    List<Event> findByDepartmentPageIdAndEventDateTimeAfterOrderByEventDateTimeDesc(
            @Param("pageId") Long pageId, @Param("now") LocalDateTime now);
}
