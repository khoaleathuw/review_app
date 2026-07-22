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

        Branch branch = findActiveBranch(
                branchCode,
                model
        );

        if (branch == null) {
            return "branch-not-found";
        }

        addBranchModel(model, branch);

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

        Branch branch = findActiveBranch(
                branchCode,
                model
        );

        if (branch == null) {
            return "branch-not-found";
        }

        if (rating == null || rating < 1 || rating > 5) {

            model.addAttribute(
                    "errorMessage",
                    "Số sao đánh giá không hợp lệ."
            );

            addBranchModel(model, branch);

            return "index";
        }

        // 1–3 sao: chuyển sang trang chọn lý do
        if (rating <= 3) {

            addBranchModel(model, branch);

            model.addAttribute(
                    "rating",
                    rating
            );

            return "low-rating";
        }

        // 4–5 sao: lưu đánh giá ngay
        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setBranchCode(branch.getCode());
        request.setRating(rating);
        request.setDeviceName(
                "iPad quầy thu ngân"
        );

        reviewService.createReview(request);

        addBranchModel(model, branch);

        model.addAttribute(
                "rating",
                rating
        );

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

        Branch branch = findActiveBranch(
                branchCode,
                model
        );

        if (branch == null) {
            return "branch-not-found";
        }

        addBranchModel(model, branch);

        model.addAttribute(
                "rating",
                rating
        );

        model.addAttribute(
                "selectedReason",
                reason
        );

        model.addAttribute(
                "otherReason",
                otherReason
        );

        model.addAttribute(
                "comment",
                comment
        );

        if (rating == null || rating < 1 || rating > 3) {

            model.addAttribute(
                    "errorMessage",
                    "Số sao đánh giá không hợp lệ."
            );

            return "low-rating";
        }

        if (reason == null || reason.isBlank()) {

            model.addAttribute(
                    "errorMessage",
                    "Vui lòng chọn lý do đánh giá."
            );

            return "low-rating";
        }

        String finalReason = reason.trim();

        if ("Lý do khác".equalsIgnoreCase(finalReason)) {

            if (otherReason == null
                    || otherReason.isBlank()) {

                model.addAttribute(
                        "errorMessage",
                        "Vui lòng nhập lý do khác."
                );

                return "low-rating";
            }

            finalReason = otherReason.trim();
        }

        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setRating(rating);
        request.setBranchCode(branch.getCode());
        request.setReason(finalReason);

        request.setComment(
                normalizeOptionalText(comment)
        );

        request.setDeviceName(
                "iPad quầy thu ngân"
        );

        reviewService.createReview(request);

        return "thank-you";
    }

    // ==================================================
    // TÌM CHI NHÁNH ĐANG HOẠT ĐỘNG
    // ==================================================

    private Branch findActiveBranch(
            String branchCode,
            Model model
    ) {

        String normalizedCode =
                normalizeBranchCode(branchCode);

        if (normalizedCode == null) {

            model.addAttribute(
                    "errorMessage",
                    "Mã chi nhánh không hợp lệ."
            );

            return null;
        }

        Branch branch = branchRepository
                .findByCodeAndActiveTrue(normalizedCode)
                .orElse(null);

        if (branch == null) {

            model.addAttribute(
                    "errorMessage",
                    "Chi nhánh "
                            + normalizedCode
                            + " không tồn tại hoặc đã bị khóa."
            );

            return null;
        }

        return branch;
    }

    // ==================================================
    // THÊM THÔNG TIN CHI NHÁNH VÀO MODEL
    // ==================================================

    private void addBranchModel(
            Model model,
            Branch branch
    ) {

        model.addAttribute(
                "branch",
                branch
        );

        model.addAttribute(
                "branchCode",
                branch.getCode()
        );

        model.addAttribute(
                "branchName",
                branch.getName()
        );
    }

    // ==================================================
    // CHUẨN HÓA MÃ CHI NHÁNH
    // ==================================================

    private String normalizeBranchCode(
            String branchCode
    ) {

        if (branchCode == null
                || branchCode.isBlank()) {

            return null;
        }

        return branchCode
                .trim()
                .toUpperCase();
    }

    // ==================================================
    // CHUẨN HÓA NỘI DUNG KHÔNG BẮT BUỘC
    // ==================================================

    private String normalizeOptionalText(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}