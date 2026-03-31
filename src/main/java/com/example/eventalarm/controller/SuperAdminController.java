package com.example.eventalarm.controller;

import com.example.eventalarm.domain.DepartmentPage;
import com.example.eventalarm.service.DepartmentPageService;
import com.example.eventalarm.service.StatsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/superadmin")
public class SuperAdminController {

    private static final String SESSION_KEY = "superadmin";

    @Value("${superadmin.password}")
    private String superAdminPassword;

    private final DepartmentPageService pageService;
    private final StatsService statsService;

    public SuperAdminController(DepartmentPageService pageService, StatsService statsService) {
        this.pageService = pageService;
        this.statsService = statsService;
    }

    private boolean isAuthenticated(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute(SESSION_KEY));
    }

    // ── 로그인 ────────────────────────────────────────────────────

    @GetMapping
    public String loginForm(HttpSession session) {
        if (isAuthenticated(session)) return "redirect:/superadmin/dashboard";
        return "superadmin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        if (!superAdminPassword.equals(password)) {
            redirectAttributes.addFlashAttribute("error", "비밀번호가 올바르지 않습니다.");
            return "redirect:/superadmin";
        }
        session.setAttribute(SESSION_KEY, true);
        return "redirect:/superadmin/dashboard";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(SESSION_KEY);
        return "redirect:/superadmin";
    }

    // ── 대시보드 ──────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!isAuthenticated(session)) return "redirect:/superadmin";
        List<DepartmentPage> pages = pageService.findAll();
        model.addAttribute("pages", pages);
        return "superadmin/dashboard";
    }

    // ── 통계 대시보드 ─────────────────────────────────────────────

    @GetMapping("/stats")
    public String stats(HttpSession session, Model model) {
        if (!isAuthenticated(session)) return "redirect:/superadmin";
        model.addAttribute("stats", statsService.getStats());
        return "superadmin/stats";
    }

    // ── 페이지 수정 ───────────────────────────────────────────────

    @GetMapping("/pages/{id}/edit")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAuthenticated(session)) return "redirect:/superadmin";
        model.addAttribute("page", pageService.findById(id));
        return "superadmin/edit";
    }

    @PostMapping("/pages/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String universityName,
                       @RequestParam String departmentName,
                       @RequestParam String slug,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(session)) return "redirect:/superadmin";
        try {
            pageService.update(id, universityName, departmentName, slug);
            redirectAttributes.addFlashAttribute("successMsg", "수정되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "페이지를 찾을 수 없습니다.");
        }
        return "redirect:/superadmin/dashboard";
    }

    // ── 페이지 삭제 ───────────────────────────────────────────────

    @PostMapping("/pages/{id}/delete")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(session)) return "redirect:/superadmin";
        pageService.delete(id);
        redirectAttributes.addFlashAttribute("successMsg", "삭제되었습니다.");
        return "redirect:/superadmin/dashboard";
    }
}
