package com.example.eventalarm.service;

import com.example.eventalarm.domain.DepartmentPage;
import com.example.eventalarm.domain.Event;
import com.example.eventalarm.dto.EventCreateDto;
import com.example.eventalarm.repository.DepartmentPageRepository;
import com.example.eventalarm.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final DepartmentPageRepository pageRepository;

    public EventService(EventRepository eventRepository, DepartmentPageRepository pageRepository) {
        this.eventRepository = eventRepository;
        this.pageRepository = pageRepository;
    }

    /** 행사 등록 */
    public Event create(Long pageId, EventCreateDto dto) {
        DepartmentPage page = pageRepository.findById(pageId)
                .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));

        Event event = new Event();
        event.setDepartmentPage(page);
        setEventFields(event, dto);
        return eventRepository.save(event);
    }

    /** 행사 수정 */
    public Event update(Long eventId, EventCreateDto dto) {
        Event event = findById(eventId);
        setEventFields(event, dto);
        return event;
    }

    /** 행사 삭제 */
    public void delete(Long eventId) {
        eventRepository.deleteById(eventId);
    }

    /** 단건 조회 */
    @Transactional(readOnly = true)
    public Event findById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("행사를 찾을 수 없습니다."));
    }

    /**
     * 목록 조회
     * @param showPast true면 지난 행사 포함, false면 미래만
     * @param sortDesc true면 오래 남은순(내림차순), false면 임박순(오름차순)
     */
    @Transactional(readOnly = true)
    public List<Event> findByPage(Long pageId, boolean showPast, boolean sortDesc) {
        if (showPast) {
            return eventRepository.findByDepartmentPageIdOrderByEventDateTimeAsc(pageId);
        }
        LocalDateTime now = LocalDateTime.now();
        if (sortDesc) {
            return eventRepository.findByDepartmentPageIdAndEventDateTimeAfterOrderByEventDateTimeDesc(pageId, now);
        }
        return eventRepository.findByDepartmentPageIdAndEventDateTimeAfterOrderByEventDateTimeAsc(pageId, now);
    }

    // ── private ──────────────────────────────────────────────────

    private void setEventFields(Event event, EventCreateDto dto) {
        event.setTitle(dto.getTitle());
        event.setEventDateTime(dto.getEventDateTime());
        event.setLocation(dto.getLocation());
        event.setLocationAddress(dto.getLocationAddress());
        event.setDescription(dto.getDescription());
        event.setFee(dto.getFee());
        event.setBankAccount(dto.getBankAccount());
    }
}
