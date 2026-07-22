package com.example.review.controller;

import com.example.review.entity.Branch;
import com.example.review.entity.Review;
import com.example.review.repository.BranchRepository;
import com.example.review.repository.ReviewRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final ReviewRepository reviewRepository;
    private final BranchRepository branchRepository;

    public AdminPageController(
            ReviewRepository reviewRepository,
            BranchRepository branchRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.branchRepository = branchRepository;
    }

    // ==================================================
    // DASHBOARD THEO MÃ CHI NHÁNH
    // Ví dụ: /admin/dashboard?branchCode=CN001
    // ==================================================

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String branchCode,
            Model model
    ) {

        // Trang HTML hiện tại là báo cáo của một chi nhánh,
        // nên nếu chưa chọn chi nhánh thì quay về danh sách.
        if (branchCode == null || branchCode.isBlank()) {
            return "redirect:/admin/branches";
        }

        String normalizedBranchCode =
                branchCode.trim().toUpperCase();

        Branch branch = branchRepository
                .findAll()
                .stream()
                .filter(item ->
                        item.getCode() != null
                                && item.getCode()
                                .trim()
                                .equalsIgnoreCase(
                                        normalizedBranchCode
                                )
                )
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy chi nhánh với mã: "
                                        + normalizedBranchCode
                        )
                );

        return renderBranchReport(branch, model);
    }

    // ==================================================
    // XEM BÁO CÁO BẰNG ID CHI NHÁNH
    // Ví dụ: /admin/branches/1/report
    // ==================================================

    @GetMapping("/branches/{id}/report")
    public String branchReport(
            @PathVariable Long id,
            Model model
    ) {

        Branch branch = branchRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy chi nhánh với ID: "
                                        + id
                        )
                );

        return renderBranchReport(branch, model);
    }

    // ==================================================
    // TẠO DỮ LIỆU BÁO CÁO
    // ==================================================

    private String renderBranchReport(
            Branch branch,
            Model model
    ) {

        if (branch.getCode() == null
                || branch.getCode().isBlank()) {

            throw new RuntimeException(
                    "Chi nhánh chưa có mã chi nhánh"
            );
        }

        String branchCode =
                branch.getCode().trim().toUpperCase();

        List<Review> reviews = reviewRepository
                .findByBranchCodeOrderByCreatedAtDesc(
                        branchCode
                );

        long totalReviews = reviews.size();

        double averageRating = reviews.stream()
                .filter(review ->
                        review.getRating() != null
                )
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        long oneStar = countByRating(reviews, 1);
        long twoStar = countByRating(reviews, 2);
        long threeStar = countByRating(reviews, 3);
        long fourStar = countByRating(reviews, 4);
        long fiveStar = countByRating(reviews, 5);

        long lowRatingCount = reviews.stream()
                .filter(review ->
                        review.getRating() != null
                                && review.getRating() <= 3
                )
                .count();

        long positiveRatingCount = reviews.stream()
                .filter(review ->
                        review.getRating() != null
                                && review.getRating() >= 4
                )
                .count();

        double satisfactionRate =
                totalReviews == 0
                        ? 0.0
                        : positiveRatingCount
                        * 100.0
                        / totalReviews;

        // Quan trọng: dashboard.html đang sử dụng ${branch}
        model.addAttribute("branch", branch);

        model.addAttribute("reviews", reviews);
        model.addAttribute("totalReviews", totalReviews);
        model.addAttribute("averageRating", averageRating);

        model.addAttribute(
                "lowRatingCount",
                lowRatingCount
        );

        model.addAttribute(
                "positiveRatingCount",
                positiveRatingCount
        );

        model.addAttribute(
                "satisfactionRate",
                satisfactionRate
        );

        model.addAttribute("oneStar", oneStar);
        model.addAttribute("twoStar", twoStar);
        model.addAttribute("threeStar", threeStar);
        model.addAttribute("fourStar", fourStar);
        model.addAttribute("fiveStar", fiveStar);

        return "admin/dashboard";
    }

    // ==================================================
    // ĐẾM ĐÁNH GIÁ THEO SỐ SAO
    // ==================================================

    private long countByRating(
            List<Review> reviews,
            int rating
    ) {

        return reviews.stream()
                .filter(review ->
                        review.getRating() != null
                                && review.getRating() == rating
                )
                .count();
    }
}