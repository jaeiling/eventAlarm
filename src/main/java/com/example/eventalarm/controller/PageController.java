package com.example.eventalarm.controller;

import com.example.eventalarm.domain.DepartmentPage;
import com.example.eventalarm.domain.Event;
import com.example.eventalarm.dto.DepartmentPageCreateDto;
import com.example.eventalarm.dto.EventCreateDto;
import com.example.eventalarm.service.DepartmentPageService;
import com.example.eventalarm.service.EventService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class PageController {

    private final DepartmentPageService pageService;
    private final EventService eventService;

    public PageController(DepartmentPageService pageService, EventService eventService) {
        this.pageService = pageService;
        this.eventService = eventService;
    }

    // ── 세션 헬퍼 ────────────────────────────────────────────────

    private static final String SESSION_KEY_PREFIX = "admin:";

    private boolean isAdminSession(HttpSession session, Long pageId) {
        return Boolean.TRUE.equals(session.getAttribute(SESSION_KEY_PREFIX + pageId));
    }

    private void setAdminSession(HttpSession session, Long pageId) {
        session.setAttribute(SESSION_KEY_PREFIX + pageId, true);
    }

    // ── 랜딩 페이지 ──────────────────────────────────────────────

    @GetMapping("/")
    public String landing(Model model) {
        model.addAttribute("pages", pageService.findAll());
        return "landing";
    }

    // ── 업로드 이미지 서빙 ───────────────────────────────────────

    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path file = Paths.get("uploads/events").resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            }
        } catch (MalformedURLException ignored) {}
        return ResponseEntity.notFound().build();
    }

    // ── 페이지 생성 (관리자) ─────────────────────────────────────

    @GetMapping("/admin/create")
    public String createPageForm(Model model) {
        model.addAttribute("dto", new DepartmentPageCreateDto());
        return "admin/create";
    }

    @PostMapping("/admin/create")
    public String createPage(@Valid @ModelAttribute("dto") DepartmentPageCreateDto dto,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) return "admin/create";
        try {
            DepartmentPage created = pageService.create(dto);
            redirectAttributes.addFlashAttribute("serialNumber", created.getSerialNumber());
            redirectAttributes.addFlashAttribute("pageId", created.getId());
            redirectAttributes.addFlashAttribute("pageSlug", created.getSlug());
            redirectAttributes.addFlashAttribute("shareUrl",
                    "https://event-alarm.up.railway.app/page/" + created.getSlug());
            return "redirect:/admin/created";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("departmentName", "duplicate", e.getMessage());
            return "admin/create";
        }
    }

    @GetMapping("/admin/created")
    public String createPageSuccess() {
        return "admin/created";
    }

    // ── 관리자 인증 ──────────────────────────────────────────────

    @GetMapping("/admin/{pageId}/verify")
    public String verifyForm(@PathVariable Long pageId,
                             @RequestParam(defaultValue = "admin") String from,
                             Model model) {
        model.addAttribute("pageId", pageId);
        model.addAttribute("from", from);
        return "admin/verify";
    }

    @PostMapping("/admin/{pageId}/verify")
    public String verify(@PathVariable Long pageId,
                         @RequestParam String serialNumber,
                         @RequestParam(defaultValue = "admin") String from,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!pageService.verifySerialNumber(pageId, serialNumber)) {
            redirectAttributes.addFlashAttribute("error", "일련번호가 올바르지 않습니다.");
            return "redirect:/admin/" + pageId + "/verify?from=" + from;
        }
        setAdminSession(session, pageId);
        if ("page".equals(from)) {
            DepartmentPage page = pageService.findById(pageId);
            String slug = page.getSlug() != null ? page.getSlug() : String.valueOf(pageId);
            return "redirect:/page/" + UriUtils.encodePathSegment(slug, StandardCharsets.UTF_8);
        }
        return "redirect:/admin/" + pageId + "/events";
    }

    // ── 관리자 행사 관리 ─────────────────────────────────────────

    @GetMapping("/admin/{pageId}/events")
    public String adminEvents(@PathVariable Long pageId,
                              @RequestParam(required = false) String serial,
                              HttpSession session,
                              Model model) {
        if (serial != null && pageService.verifySerialNumber(pageId, serial)) {
            setAdminSession(session, pageId);
        }
        if (!isAdminSession(session, pageId)) {
            return "redirect:/admin/" + pageId + "/verify";
        }
        DepartmentPage page = pageService.findById(pageId);
        List<Event> events = eventService.findByPage(pageId, true, false);
        model.addAttribute("page", page);
        model.addAttribute("events", events);
        return "admin/events";
    }

    @GetMapping("/admin/{pageId}/events/new")
    public String newEventForm(@PathVariable Long pageId,
                               HttpSession session,
                               Model model) {
        if (!isAdminSession(session, pageId)) {
            return "redirect:/admin/" + pageId + "/verify";
        }
        DepartmentPage page = pageService.findById(pageId);
        model.addAttribute("page", page);
        model.addAttribute("newEventDto", new EventCreateDto());
        return "admin/new";
    }

    @PostMapping(value = "/admin/{pageId}/events/create", consumes = {"multipart/form-data"})
    public String createEvent(@PathVariable Long pageId,
                              HttpSession session,
                              @Valid @ModelAttribute("newEventDto") EventCreateDto dto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (!isAdminSession(session, pageId)) {
            return "redirect:/admin/" + pageId + "/verify";
        }
        if (bindingResult.hasErrors()) {
            DepartmentPage page = pageService.findById(pageId);
            model.addAttribute("page", page);
            return "admin/new";
        }
        eventService.create(pageId, dto);
        redirectAttributes.addFlashAttribute("successMsg", "행사가 등록되었습니다.");
        return "redirect:/admin/" + pageId + "/events";
    }

    @PostMapping("/admin/{pageId}/events/{eventId}/delete")
    public String deleteEventAdmin(@PathVariable Long pageId,
                                   @PathVariable Long eventId,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (!isAdminSession(session, pageId)) {
            return "redirect:/admin/" + pageId + "/verify";
        }
        eventService.delete(eventId);
        redirectAttributes.addFlashAttribute("successMsg", "행사가 삭제되었습니다.");
        return "redirect:/admin/" + pageId + "/events";
    }

    // ── 메인 페이지 (공개) ───────────────────────────────────────

    @GetMapping("/page/{slug}")
    public String mainPageBySlug(@PathVariable String slug,
                                 @RequestParam(defaultValue = "false") boolean showPast,
                                 @RequestParam(defaultValue = "false") boolean sortDesc,
                                 HttpSession session,
                                 Model model) {
        DepartmentPage page;
        try {
            page = resolvePageBySlug(slug);
        } catch (IllegalArgumentException e) {
            return "redirect:/";
        }

        // 숫자 ID로 접근 시 slug URL로 301 리다이렉트
        if (slug.matches("\\d+") && page.getSlug() != null) {
            return "redirect:/page/" + UriUtils.encodePathSegment(page.getSlug(), StandardCharsets.UTF_8);
        }

        List<Event> events = eventService.findByPage(page.getId(), showPast, sortDesc);
        List<Event> allCalendarEvents = eventService.findAllEventsForCalendar(page.getId());
        boolean isAdmin = isAdminSession(session, page.getId());

        model.addAttribute("page", page);
        model.addAttribute("events", events);
        model.addAttribute("allCalendarEvents", allCalendarEvents);
        model.addAttribute("showPast", showPast);
        model.addAttribute("sortDesc", sortDesc);
        model.addAttribute("isAdmin", isAdmin);
        return "main/page";
    }

    @GetMapping("/page/{slug}/events/{eventId}/edit")
    public String editEventForm(@PathVariable String slug,
                                @PathVariable Long eventId,
                                HttpSession session,
                                Model model) {
        DepartmentPage page = resolvePageBySlug(slug);
        if (!isAdminSession(session, page.getId())) {
            return "redirect:/page/" + UriUtils.encodePathSegment(page.getSlug(), StandardCharsets.UTF_8);
        }
        Event event = eventService.findById(eventId);
        EventCreateDto dto = toDto(event);
        model.addAttribute("page", page);
        model.addAttribute("event", event);
        model.addAttribute("dto", dto);
        return "main/edit";
    }

    @PostMapping(value = "/page/{slug}/events/{eventId}/edit", consumes = {"multipart/form-data"})
    public String editEvent(@PathVariable String slug,
                            @PathVariable Long eventId,
                            HttpSession session,
                            @Valid @ModelAttribute("dto") EventCreateDto dto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        DepartmentPage page = resolvePageBySlug(slug);
        if (!isAdminSession(session, page.getId())) {
            return "redirect:/page/" + UriUtils.encodePathSegment(page.getSlug(), StandardCharsets.UTF_8);
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("page", page);
            model.addAttribute("event", eventService.findById(eventId));
            return "main/edit";
        }
        eventService.update(eventId, dto);
        redirectAttributes.addFlashAttribute("successMsg", "행사가 수정되었습니다.");
        return "redirect:/admin/" + page.getId() + "/events";
    }

    @PostMapping("/page/{slug}/events/{eventId}/delete")
    public String deleteEventMain(@PathVariable String slug,
                                  @PathVariable Long eventId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        DepartmentPage page = resolvePageBySlug(slug);
        if (!isAdminSession(session, page.getId())) {
            return "redirect:/page/" + UriUtils.encodePathSegment(page.getSlug(), StandardCharsets.UTF_8);
        }
        eventService.delete(eventId);
        redirectAttributes.addFlashAttribute("successMsg", "행사가 삭제되었습니다.");
        return "redirect:/page/" + UriUtils.encodePathSegment(page.getSlug(), StandardCharsets.UTF_8);
    }

    @PostMapping("/page/{slug}/logout")
    public String logoutPage(@PathVariable String slug, HttpSession session) {
        DepartmentPage page;
        try { page = resolvePageBySlug(slug); } catch (IllegalArgumentException e) { return "redirect:/"; }
        session.removeAttribute(SESSION_KEY_PREFIX + page.getId());
        return "redirect:/page/" + UriUtils.encodePathSegment(page.getSlug(), StandardCharsets.UTF_8);
    }

    // ── private ──────────────────────────────────────────────────

    private DepartmentPage resolvePageBySlug(String slug) {
        if (slug.matches("\\d+")) {
            return pageService.findById(Long.parseLong(slug));
        }
        return pageService.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));
    }

    private EventCreateDto toDto(Event event) {
        EventCreateDto dto = new EventCreateDto();
        dto.setTitle(event.getTitle());
        dto.setPostType(event.getPostType());
        dto.setEventDateTime(event.getEventDateTime());
        dto.setEventEndDateTime(event.getEventEndDateTime());
        dto.setLocation(event.getLocation());
        dto.setLocationAddress(event.getLocationAddress());
        dto.setDescription(event.getDescription());
        dto.setFee(event.getFee());
        dto.setBankAccount(event.getBankAccount());
        dto.setLink(event.getLink());
        return dto;
    }
}
