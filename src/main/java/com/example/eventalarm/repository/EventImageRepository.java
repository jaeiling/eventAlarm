package com.example.eventalarm.repository;

import com.example.eventalarm.domain.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {
    List<EventImage> findByEventIdOrderBySortOrderAsc(Long eventId);
    void deleteByEventId(Long eventId);
}
