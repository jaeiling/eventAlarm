package com.example.eventalarm.repository;

import com.example.eventalarm.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 해당 학과 페이지의 모든 행사 (날짜 오름차순)
    List<Event> findByDepartmentPageIdOrderByEventDateTimeAsc(Long departmentPageId);

    // 해당 학과 페이지의 미래 행사만 (날짜 오름차순)
    List<Event> findByDepartmentPageIdAndEventDateTimeAfterOrderByEventDateTimeAsc(
            Long departmentPageId, LocalDateTime now);

    // 해당 학과 페이지의 미래 행사만 (날짜 내림차순 - 오래 남은순)
    List<Event> findByDepartmentPageIdAndEventDateTimeAfterOrderByEventDateTimeDesc(
            Long departmentPageId, LocalDateTime now);
}
