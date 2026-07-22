package com.example.review.controller;

import com.example.review.entity.Review;
import com.example.review.service.ReviewService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(
            ReviewService reviewService
    ) {
        this.reviewService = reviewService;
    }

    // ==========================================
    // MỞ TRANG SỬA ĐÁNH GIÁ
    // ==========================================

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {

            Review review =
                    reviewService.getByIdForManagement(
                            id,
                            authentication
                    );

            model.addAttribute(
                    "review",
                    review
            );

            return "admin/reviews/form";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/dashboard";
        }
    }

    // ==========================================
    // CẬP NHẬT ĐÁNH GIÁ
    // ==========================================

    @PostMapping("/{id}/edit")
    public String updateReview(
            @PathVariable Long id,
            @RequestParam Integer rating,
            @RequestParam(required = false)
            String reason,
            @RequestParam(required = false)
            String comment,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {

            Review review =
                    reviewService.updateReview(
                            id,
                            rating,
                            reason,
                            comment,
                            authentication
                    );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Cập nhật đánh giá thành công"
            );

            Long branchId =
                    review.getBranch().getId();

            return "redirect:/admin/branches/"
                    + branchId
                    + "/report";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/reviews/"
                    + id
                    + "/edit";
        }
    }

    // ==========================================
    // XÓA ĐÁNH GIÁ
    // ==========================================

    @PostMapping("/{id}/delete")
    public String deleteReview(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {

            Long branchId =
                    reviewService.deleteReview(
                            id,
                            authentication
                    );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Đã xóa đánh giá"
            );

            return "redirect:/admin/branches/"
                    + branchId
                    + "/report";

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/dashboard";
        }
    }
}