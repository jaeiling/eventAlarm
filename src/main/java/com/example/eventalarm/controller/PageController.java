package com.example.eventalarm.controller;

import com.example.eventalarm.domain.DepartmentPage;
import com.example.eventalarm.domain.Event;
import com.example.eventalarm.dto.DepartmentPageCreateDto;
import com.example.eventalarm.dto.EventCreateDto;
import com.example.eventalarm.service.DepartmentPageService;
import com.example.eventalarm.service.EventService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String landing() {
        return "landing";
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
        if (bindingResult.hasErrors()) {
            return "admin/create";
        }
        DepartmentPage created = pageService.create(dto);
        redirectAttributes.addFlashAttribute("serialNumber", created.getSerialNumber());
        redirectAttributes.addFlashAttribute("pageId", created.getId());
        return "redirect:/admin/created";
    }

    @GetMapping("/admin/created")
    public String createPageSuccess() {
        return "admin/created";
    }

    // ── 관리자 행사 관리 (일련번호 인증 후) ─────────────────────

    @GetMapping("/admin/{pageId}/verify")
    public String verifyForm(@PathVariable Long pageId, Model model) {
        model.addAttribute("pageId", pageId);
        return "admin/verify";
    }

    @PostMapping("/admin/{pageId}/verify")
    public String verify(@PathVariable Long pageId,
                         @RequestParam String serialNumber,
                         RedirectAttributes redirectAttributes) {
        if (!pageService.verifySerialNumber(pageId, serialNumber)) {
            redirectAttributes.addFlashAttribute("error", "일련번호가 올바르지 않습니다.");
            return "redirect:/admin/" + pageId + "/verify";
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

    @PostMapping("/admin/{pageId}/events/create")
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
    public String deleteEvent(@PathVariable Long pageId,
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

    // ── 메인 페이지 (공개) ───────────────────────────────────────

    @GetMapping("/page/{pageId}")
    public String mainPage(@PathVariable Long pageId,
                           @RequestParam(defaultValue = "false") boolean showPast,
                           @RequestParam(defaultValue = "false") boolean sortDesc,
                           Model model) {
        DepartmentPage page = pageService.findById(pageId);
        List<Event> events = eventService.findByPage(pageId, showPast, sortDesc);
        model.addAttribute("page", page);
        model.addAttribute("events", events);
        model.addAttribute("showPast", showPast);
        model.addAttribute("sortDesc", sortDesc);
        return "main/page";
    }
}
