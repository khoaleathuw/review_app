package com.example.review.controller;

import com.example.review.entity.Branch;
import com.example.review.service.BranchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/branches")
public class AdminBranchController {

    private final BranchService branchService;

    public AdminBranchController(
            BranchService branchService
    ) {
        this.branchService = branchService;
    }

    // =========================
    // DANH SÁCH CHI NHÁNH
    // =========================

    @GetMapping
    public String branchList(Model model) {

        model.addAttribute(
                "branches",
                branchService.getAllBranches()
        );

        return "admin/branches";
    }

    // =========================
    // FORM THÊM
    // =========================

    @GetMapping("/new")
    public String createForm(Model model) {

        model.addAttribute("branch", new Branch());
        model.addAttribute("formTitle", "Thêm chi nhánh");
        model.addAttribute("editMode", false);

        return "admin/branch-form";
    }

    // =========================
    // LƯU CHI NHÁNH MỚI
    // =========================

    @PostMapping("/create")
    public String createBranch(
            @RequestParam String name,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes
    ) {

        try {

            Branch branch = branchService.createBranch(
                    name,
                    address
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã tạo chi nhánh " + branch.getCode()
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/admin/branches/new";
        }

        return "redirect:/admin/branches";
    }

    // =========================
    // FORM SỬA
    // =========================

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model
    ) {

        Branch branch = branchService.getById(id);

        model.addAttribute("branch", branch);
        model.addAttribute("formTitle", "Chỉnh sửa chi nhánh");
        model.addAttribute("editMode", true);

        return "admin/branch-form";
    }

    // =========================
    // CẬP NHẬT
    // =========================

    @PostMapping("/{id}/update")
    public String updateBranch(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes
    ) {

        try {

            branchService.updateBranch(
                    id,
                    name,
                    address
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã cập nhật chi nhánh"
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "redirect:/admin/branches/"
                    + id
                    + "/edit";
        }

        return "redirect:/admin/branches";
    }

    // =========================
    // KHÓA / MỞ CHI NHÁNH
    // =========================

    @PostMapping("/{id}/toggle")
    public String toggleBranch(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        Branch branch = branchService.toggleStatus(id);

        String message = Boolean.TRUE.equals(branch.getActive())
                ? "Đã mở lại chi nhánh " + branch.getCode()
                : "Đã khóa chi nhánh " + branch.getCode();

        redirectAttributes.addFlashAttribute(
                "successMessage",
                message
        );

        return "redirect:/admin/branches";
    }
}