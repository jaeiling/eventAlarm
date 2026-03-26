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

    /**
     * 목록 조회
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

    private void saveImages(Event event, EventCreateDto dto) {
        List<MultipartFile> files = dto.getImages();
        if (files == null || files.isEmpty()) return;

        int thumbnailIndex = dto.getThumbnailIndex();
        int order = 0;
        for (int i = 0; i < Math.min(files.size(), 5); i++) {
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
