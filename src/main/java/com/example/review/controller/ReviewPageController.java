package com.example.review.controller;

import com.example.review.dto.CreateReviewRequest;
import com.example.review.entity.Branch;
import com.example.review.repository.BranchRepository;
import com.example.review.service.ReviewService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ReviewPageController {

    private final ReviewService reviewService;
    private final BranchRepository branchRepository;

    public ReviewPageController(
            ReviewService reviewService,
            BranchRepository branchRepository
    ) {
        this.reviewService = reviewService;
        this.branchRepository = branchRepository;
    }

    // ==================================================
    // TRANG ĐÁNH GIÁ THEO CHI NHÁNH
    // Ví dụ: /review/CN001
    // ==================================================

    @GetMapping("/review/{branchCode}")
    public String reviewPage(
            @PathVariable String branchCode,
            Model model
    ) {

        String normalizedCode = normalizeBranchCode(branchCode);

        if (normalizedCode == null) {
            model.addAttribute(
                    "errorMessage",
                    "Mã chi nhánh không hợp lệ."
            );

            return "branch-not-found";
        }

        Branch branch = branchRepository
                .findByCodeAndActiveTrue(normalizedCode)
                .orElse(null);

        if (branch == null) {
            model.addAttribute(
                    "errorMessage",
                    "Chi nhánh " + normalizedCode
                            + " không tồn tại hoặc đã bị khóa."
            );

            return "branch-not-found";
        }

        model.addAttribute("branch", branch);
        model.addAttribute("branchCode", branch.getCode());
        model.addAttribute("branchName", branch.getName());

        return "index";
    }

    // ==================================================
    // KHÁCH CHỌN SỐ SAO
    // ==================================================

    @PostMapping("/rating")
    public String selectRating(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String branchCode,
            Model model
    ) {

        String normalizedCode = normalizeBranchCode(branchCode);

        if (normalizedCode == null) {
            model.addAttribute(
                    "errorMessage",
                    "Mã chi nhánh không hợp lệ."
            );

            return "branch-not-found";
        }

        Branch branch = branchRepository
                .findByCodeAndActiveTrue(normalizedCode)
                .orElse(null);

        if (branch == null) {
            model.addAttribute(
                    "errorMessage",
                    "Chi nhánh " + normalizedCode
                            + " không tồn tại hoặc đã bị khóa."
            );

            return "branch-not-found";
        }

        if (rating == null || rating < 1 || rating > 5) {
            model.addAttribute(
                    "errorMessage",
                    "Số sao đánh giá không hợp lệ."
            );

            return "branch-not-found";
        }

        // 1–3 sao: chuyển sang trang chọn lý do
        if (rating <= 3) {
            model.addAttribute("rating", rating);
            model.addAttribute("branchCode", branch.getCode());
            model.addAttribute("branchName", branch.getName());

            return "low-rating";
        }

        // 4–5 sao: lưu đánh giá ngay
        CreateReviewRequest request = new CreateReviewRequest();

        request.setBranchCode(branch.getCode());
        request.setRating(rating);
        request.setDeviceName("iPad quầy thu ngân");

        reviewService.createReview(request);

        model.addAttribute("rating", rating);
        model.addAttribute("branchCode", branch.getCode());
        model.addAttribute("branchName", branch.getName());

        return "thank-you";
    }

    // ==================================================
    // GỬI ĐÁNH GIÁ 1–3 SAO
    // ==================================================

    @PostMapping("/review/submit")
    public String submitLowRating(
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String branchCode,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String otherReason,
            @RequestParam(required = false) String comment,
            Model model
    ) {

        String normalizedCode = normalizeBranchCode(branchCode);

        if (normalizedCode == null) {
            model.addAttribute(
                    "errorMessage",
                    "Mã chi nhánh không hợp lệ."
            );

            return "branch-not-found";
        }

        Branch branch = branchRepository
                .findByCodeAndActiveTrue(normalizedCode)
                .orElse(null);

        if (branch == null) {
            model.addAttribute(
                    "errorMessage",
                    "Chi nhánh " + normalizedCode
                            + " không tồn tại hoặc đã bị khóa."
            );

            return "branch-not-found";
        }

        if (rating == null || rating < 1 || rating > 3) {
            model.addAttribute(
                    "errorMessage",
                    "Số sao đánh giá không hợp lệ."
            );

            return "branch-not-found";
        }

        if (reason == null || reason.isBlank()) {
            model.addAttribute(
                    "errorMessage",
                    "Vui lòng chọn lý do đánh giá."
            );

            model.addAttribute("rating", rating);
            model.addAttribute("branchCode", branch.getCode());
            model.addAttribute("branchName", branch.getName());

            return "low-rating";
        }

        String finalReason = reason.trim();

        if ("Lý do khác".equalsIgnoreCase(reason.trim())) {

            if (otherReason == null || otherReason.isBlank()) {
                model.addAttribute(
                        "errorMessage",
                        "Vui lòng nhập lý do khác."
                );

                model.addAttribute("rating", rating);
                model.addAttribute("branchCode", branch.getCode());
                model.addAttribute("branchName", branch.getName());

                return "low-rating";
            }

            finalReason = otherReason.trim();
        }

        CreateReviewRequest request = new CreateReviewRequest();

        request.setRating(rating);
        request.setBranchCode(branch.getCode());
        request.setReason(finalReason);
        request.setComment(
                comment == null || comment.isBlank()
                        ? null
                        : comment.trim()
        );
        request.setDeviceName("iPad quầy thu ngân");

        reviewService.createReview(request);

        model.addAttribute("rating", rating);
        model.addAttribute("branchCode", branch.getCode());
        model.addAttribute("branchName", branch.getName());

        return "thank-you";
    }

    // ==================================================
    // CHUẨN HÓA MÃ CHI NHÁNH
    // ==================================================

    private String normalizeBranchCode(String branchCode) {

        if (branchCode == null || branchCode.isBlank()) {
            return null;
        }

        return branchCode
                .trim()
                .toUpperCase();
    }
}