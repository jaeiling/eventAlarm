package com.example.eventalarm.controller;

import com.example.eventalarm.domain.DepartmentPage;
import com.example.eventalarm.domain.Event;
import com.example.eventalarm.dto.DepartmentPageCreateDto;
import com.example.eventalarm.dto.EventCreateDto;
import com.example.eventalarm.service.DepartmentPageService;
import com.example.eventalarm.service.EventService;
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

import java.net.MalformedURLException;
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
        DepartmentPage created = pageService.create(dto);
        redirectAttributes.addFlashAttribute("serialNumber", created.getSerialNumber());
        redirectAttributes.addFlashAttribute("pageId", created.getId());
        redirectAttributes.addFlashAttribute("pageSlug", created.getSlug());
        redirectAttributes.addFlashAttribute("shareUrl",
                "https://event-alarm.up.railway.app/page/" + created.getSlug());
        return "redirect:/admin/created";
    }

    @GetMapping("/admin/created")
    public String createPageSuccess() {
        return "admin/created";
    }

    // ── 관리자 행사 관리 ─────────────────────────────────────────

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
                         RedirectAttributes redirectAttributes) {
        if (!pageService.verifySerialNumber(pageId, serialNumber)) {
            redirectAttributes.addFlashAttribute("error", "일련번호가 올바르지 않습니다.");
            return "redirect:/admin/" + pageId + "/verify?from=" + from;
        }
        // from=page 이면 메인 페이지로, 아니면 관리자 행사 등록 페이지로
        if ("page".equals(from)) {
            return "redirect:/page/" + pageId + "?serial=" + serialNumber;
        }
        return "redirect:/admin/" + pageId + "/events?serial=" + serialNumber;
    }

    @GetMapping("/admin/{pageId}/events")
    public String adminEvents(@PathVariable Long pageId,
                              @RequestParam String serial,
                              Model model) {
        if (!pageService.verifySerialNumber(pageId, serial)) {
            return "redirect:/admin/" + pageId + "/verify";
        }
        DepartmentPage page = pageService.findById(pageId);
        List<Event> events = eventService.findByPage(pageId, true, false);
        model.addAttribute("page", page);
        model.addAttribute("events", events);
        model.addAttribute("serial", serial);
        model.addAttribute("newEventDto", new EventCreateDto());
        return "admin/events";
    }

    @PostMapping(value = "/admin/{pageId}/events/create", consumes = {"multipart/form-data"})
    public String createEvent(@PathVariable Long pageId,
                              @RequestParam String serial,
                              @Valid @ModelAttribute("newEventDto") EventCreateDto dto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (!pageService.verifySerialNumber(pageId, serial)) {
            return "redirect:/admin/" + pageId + "/verify";
        }
        if (bindingResult.hasErrors()) {
            DepartmentPage page = pageService.findById(pageId);
            List<Event> events = eventService.findByPage(pageId, true, false);
            model.addAttribute("page", page);
            model.addAttribute("events", events);
            model.addAttribute("serial", serial);
            return "admin/events";
        }
        eventService.create(pageId, dto);
        redirectAttributes.addFlashAttribute("successMsg", "행사가 등록되었습니다.");
        return "redirect:/admin/" + pageId + "/events?serial=" + serial;
    }

    @PostMapping("/admin/{pageId}/events/{eventId}/delete")
    public String deleteEventAdmin(@PathVariable Long pageId,
                                   @PathVariable Long eventId,
                                   @RequestParam String serial,
                                   RedirectAttributes redirectAttributes) {
        if (!pageService.verifySerialNumber(pageId, serial)) {
            return "redirect:/admin/" + pageId + "/verify";
        }
        eventService.delete(eventId);
        redirectAttributes.addFlashAttribute("successMsg", "행사가 삭제되었습니다.");
        return "redirect:/admin/" + pageId + "/events?serial=" + serial;
    }

    // ── 메인 페이지 (공개) - slug URL ────────────────────────────

    @GetMapping("/page/{slug}")
    public String mainPageBySlug(@PathVariable String slug,
                                 @RequestParam(defaultValue = "false") boolean showPast,
                                 @RequestParam(defaultValue = "false") boolean sortDesc,
                                 @RequestParam(required = false) String serial,
                                 Model model) {
        // slug가 숫자면 기존 ID 방식으로 처리
        DepartmentPage page;
        if (slug.matches("\\d+")) {
            page = pageService.findById(Long.parseLong(slug));
        } else {
            page = pageService.findBySlug(slug)
                    .orElseThrow(() -> new IllegalArgumentException("페이지를 찾을 수 없습니다."));
        }

        List<Event> events = eventService.findByPage(page.getId(), showPast, sortDesc);
        boolean isAdmin = serial != null && pageService.verifySerialNumber(page.getId(), serial);

        model.addAttribute("page", page);
        model.addAttribute("events", events);
        model.addAttribute("showPast", showPast);
        model.addAttribute("sortDesc", sortDesc);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("serial", serial != null ? serial : "");
        return "main/page";
    }

    // ── 메인 페이지 - 행사 수정 ──────────────────────────────────

    @GetMapping("/page/{pageId}/events/{eventId}/edit")
    public String editEventForm(@PathVariable Long pageId,
                                @PathVariable Long eventId,
                                @RequestParam String serial,
                                Model model) {
        if (!pageService.verifySerialNumber(pageId, serial)) {
            return "redirect:/page/" + pageId;
        }
        Event event = eventService.findById(eventId);
        EventCreateDto dto = toDto(event);
        model.addAttribute("page", pageService.findById(pageId));
        model.addAttribute("event", event);
        model.addAttribute("dto", dto);
        model.addAttribute("serial", serial);
        return "main/edit";
    }

    @PostMapping(value = "/page/{pageId}/events/{eventId}/edit", consumes = {"multipart/form-data"})
    public String editEvent(@PathVariable Long pageId,
                            @PathVariable Long eventId,
                            @RequestParam String serial,
                            @Valid @ModelAttribute("dto") EventCreateDto dto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (!pageService.verifySerialNumber(pageId, serial)) {
            return "redirect:/page/" + pageId;
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("page", pageService.findById(pageId));
            model.addAttribute("event", eventService.findById(eventId));
            model.addAttribute("serial", serial);
            return "main/edit";
        }
        eventService.update(eventId, dto);
        redirectAttributes.addFlashAttribute("successMsg", "행사가 수정되었습니다.");
        return "redirect:/page/" + pageId + "?serial=" + serial;
    }

    // ── 메인 페이지 - 행사 삭제 ──────────────────────────────────

    @PostMapping("/page/{pageId}/events/{eventId}/delete")
    public String deleteEventMain(@PathVariable Long pageId,
                                  @PathVariable Long eventId,
                                  @RequestParam String serial,
                                  RedirectAttributes redirectAttributes) {
        if (!pageService.verifySerialNumber(pageId, serial)) {
            return "redirect:/page/" + pageId;
        }
        eventService.delete(eventId);
        redirectAttributes.addFlashAttribute("successMsg", "행사가 삭제되었습니다.");
        return "redirect:/page/" + pageId + "?serial=" + serial;
    }

    // ── private ──────────────────────────────────────────────────

    private EventCreateDto toDto(Event event) {
        EventCreateDto dto = new EventCreateDto();
        dto.setTitle(event.getTitle());
        dto.setEventDateTime(event.getEventDateTime());
        dto.setEventEndDateTime(event.getEventEndDateTime());
        dto.setLocation(event.getLocation());
        dto.setLocationAddress(event.getLocationAddress());
        dto.setDescription(event.getDescription());
        dto.setFee(event.getFee());
        dto.setBankAccount(event.getBankAccount());
        return dto;
    }
}
