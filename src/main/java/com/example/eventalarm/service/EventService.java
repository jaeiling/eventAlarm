package com.example.eventalarm.service;

import com.example.eventalarm.domain.DepartmentPage;
import com.example.eventalarm.domain.Event;
import com.example.eventalarm.domain.EventImage;
import com.example.eventalarm.dto.EventCreateDto;
import com.example.eventalarm.repository.DepartmentPageRepository;
import com.example.eventalarm.repository.EventImageRepository;
import com.example.eventalarm.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final DepartmentPageRepository pageRepository;
    private final EventImageRepository imageRepository;
    private final ImageStorageService storageService;

    public EventService(EventRepository eventRepository,
                        DepartmentPageRepository pageRepository,
                        EventImageRepository imageRepository,
                        ImageStorageService storageService) {
        this.eventRepository = eventRepository;
        this.pageRepository = pageRepository;
        this.imageRepository = imageRepository;
        this.storageService = storageService;
    }

    /** 행사 등록 */
    public Event create(Long pageId, EventCreateDto dto) {
        DepartmentPage page = pageRepository.findById(pageId)
                .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));

        Event event = new Event();
        event.setDepartmentPage(page);
        setEventFields(event, dto);
        eventRepository.save(event);

        // 이미지 저장
        saveImages(event, dto);
        return event;
    }

    /** 행사 수정 */
    public Event update(Long eventId, EventCreateDto dto) {
        Event event = findById(eventId);
        setEventFields(event, dto);

        // 새 이미지가 있으면 기존 이미지 삭제 후 재등록
        if (dto.getImages() != null && dto.getImages().stream().anyMatch(f -> !f.isEmpty())) {
            // 기존 이미지 파일 삭제
            imageRepository.findByEventIdOrderBySortOrderAsc(eventId)
                    .forEach(img -> storageService.delete(img.getStoredFileName()));
            imageRepository.deleteByEventId(eventId);
            event.getImages().clear();
            saveImages(event, dto);
        }
        return event;
    }

    /** 행사 삭제 */
    public void delete(Long eventId) {
        // 이미지 파일 먼저 삭제
        imageRepository.findByEventIdOrderBySortOrderAsc(eventId)
                .forEach(img -> storageService.delete(img.getStoredFileName()));
        eventRepository.deleteById(eventId);
    }

    /** 단건 조회 */
    @Transactional(readOnly = true)
    public Event findById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("행사를 찾을 수 없습니다."));
    }

    /** 달력용 전체 행사 조회 (지난 것 포함, NOTICE 제외) */
    @Transactional(readOnly = true)
    public List<Event> findAllEventsForCalendar(Long pageId) {
        return eventRepository.findEventsByDepartmentPageIdOrderByEventDateTimeAsc(pageId);
    }

    /**
     * 목록 조회 — 공지글은 항상 상단 고정, 그 아래에 행사 목록
     */
    @Transactional(readOnly = true)
    public List<Event> findByPage(Long pageId, boolean showPast, boolean sortDesc) {
        List<Event> notices = eventRepository.findNoticesByDepartmentPageIdOrderByCreatedAtDesc(pageId);

        List<Event> events;
        if (showPast) {
            events = eventRepository.findEventsByDepartmentPageIdOrderByEventDateTimeAsc(pageId);
        } else {
            LocalDateTime now = LocalDateTime.now();
            events = sortDesc
                ? eventRepository.findEventsByDepartmentPageIdAndEventDateTimeAfterOrderByEventDateTimeDesc(pageId, now)
                : eventRepository.findEventsByDepartmentPageIdAndEventDateTimeAfterOrderByEventDateTimeAsc(pageId, now);
        }

        List<Event> result = new java.util.ArrayList<>(notices);
        result.addAll(events);
        return result;
    }

    // ── private ──────────────────────────────────────────────────

    private void setEventFields(Event event, EventCreateDto dto) {
        String postType = dto.getPostType();
        event.setTitle(dto.getTitle());
        event.setPostType(postType);

        // EVENT 타입은 날짜 필수, NOTICE 타입은 날짜 없어도 됨
        if ("EVENT".equals(postType)) {
            if (dto.getEventDateTime() == null) {
                throw new IllegalArgumentException("행사 시작 일시를 입력해주세요.");
            }
            event.setEventDateTime(dto.getEventDateTime());
            event.setEventEndDateTime(dto.getEventEndDateTime());
            event.setLocation(dto.getLocation());
            event.setLocationAddress(dto.getLocationAddress());
        } else {
            // NOTICE: 날짜/장소 필드 null로 명시
            event.setEventDateTime(null);
            event.setEventEndDateTime(null);
            event.setLocation(null);
            event.setLocationAddress(null);
        }

        event.setDescription(dto.getDescription());
        event.setFee(dto.getFee());
        event.setBankAccount(dto.getBankAccount());
        event.setLink(dto.getLink());
    }

    private void saveImages(Event event, EventCreateDto dto) {
        List<MultipartFile> files = dto.getImages();
        if (files == null || files.isEmpty()) return;

        int thumbnailIndex = dto.getThumbnailIndex();
        int order = 0;
        for (int i = 0; i < Math.min(files.size(), 10); i++) {
            MultipartFile file = files.get(i);
            if (file == null || file.isEmpty()) continue;

            String stored = storageService.store(file);
            if (stored == null) continue;

            EventImage img = new EventImage();
            img.setEvent(event);
            img.setStoredFileName(stored);
            img.setOriginalFileName(file.getOriginalFilename());
            img.setThumbnail(i == thumbnailIndex);
            img.setSortOrder(order++);
            imageRepository.save(img);
        }
    }
}
