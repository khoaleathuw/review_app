package com.example.review.controller;

import com.example.review.entity.Branch;
import com.example.review.entity.Review;
import com.example.review.entity.User;
import com.example.review.repository.BranchRepository;
import com.example.review.repository.ReviewRepository;
import com.example.review.repository.UserRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    private final ReviewRepository reviewRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    public AdminPageController(
            ReviewRepository reviewRepository,
            BranchRepository branchRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
    }

    // ==================================================
    // DASHBOARD
    // MANAGER: chọn chi nhánh bằng branchCode
    // LEADER / EMPLOYEE: chỉ xem chi nhánh được gán
    // ==================================================

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String branchCode,
            Authentication authentication,
            Model model
    ) {

        User currentUser = getCurrentUser(authentication);

        if (isManager(currentUser)) {

            if (branchCode == null || branchCode.isBlank()) {
                return "redirect:/admin/branches";
            }

            String normalizedCode = normalizeBranchCode(branchCode);

            Branch branch = branchRepository
                    .findByCodeIgnoreCase(normalizedCode)
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Không tìm thấy chi nhánh với mã: "
                                            + normalizedCode
                            )
                    );

            return renderBranchReport(
                    branch,
                    currentUser,
                    model
            );
        }

        Branch userBranch = requireUserBranch(currentUser);

        return renderBranchReport(
                userBranch,
                currentUser,
                model
        );
    }

    // ==================================================
    // XEM BÁO CÁO THEO ID CHI NHÁNH
    // ==================================================

    @GetMapping("/branches/{id}/report")
    public String branchReport(
            @PathVariable Long id,
            Authentication authentication,
            Model model
    ) {

        User currentUser = getCurrentUser(authentication);

        Branch requestedBranch = branchRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy chi nhánh với ID: " + id
                        )
                );

        checkBranchPermission(
                currentUser,
                requestedBranch
        );

        return renderBranchReport(
                requestedBranch,
                currentUser,
                model
        );
    }
    
    // ==================================================
    // TẠO DỮ LIỆU BÁO CÁO
    // ==================================================

    private String renderBranchReport(
            Branch branch,
            User currentUser,
            Model model
    ) {

        if (branch == null) {
            throw new IllegalArgumentException(
                    "Chi nhánh không hợp lệ"
            );
        }

        if (branch.getCode() == null
                || branch.getCode().isBlank()) {

            throw new IllegalStateException(
                    "Chi nhánh chưa có mã chi nhánh"
            );
        }

        String branchCode =
                normalizeBranchCode(branch.getCode());

        List<Review> reviews = reviewRepository
                .findByBranch_CodeOrderByCreatedAtDesc(
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
                        : positiveRatingCount * 100.0
                        / totalReviews;

        model.addAttribute("branch", branch);
        model.addAttribute("reviews", reviews);

        model.addAttribute(
                "currentUser",
                currentUser
        );

        model.addAttribute(
                "currentRole",
                getRoleName(currentUser)
        );

        model.addAttribute(
                "canEdit",
                canEdit(currentUser)
        );

        model.addAttribute(
                "totalReviews",
                totalReviews
        );

        model.addAttribute(
                "averageRating",
                averageRating
        );

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
    // LẤY USER ĐANG ĐĂNG NHẬP
    // ==================================================

    private User getCurrentUser(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equalsIgnoreCase(
                        authentication.getName()
                )) {

            throw new AccessDeniedException(
                    "Bạn chưa đăng nhập"
            );
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Không tìm thấy tài khoản: "
                                        + authentication.getName()
                        )
                );

        if (user.getStatus() == null
                || !"ACTIVE".equalsIgnoreCase(
                        user.getStatus().name()
                )) {

            throw new AccessDeniedException(
                    "Tài khoản đã bị khóa"
            );
        }

        if (user.getRole() == null) {
            throw new AccessDeniedException(
                    "Tài khoản chưa được gán quyền"
            );
        }

        return user;
    }

    // ==================================================
    // KIỂM TRA QUYỀN CHI NHÁNH
    // ==================================================

    private void checkBranchPermission(
            User user,
            Branch requestedBranch
    ) {

        if (isManager(user)) {
            return;
        }

        Branch userBranch = requireUserBranch(user);

        if (!userBranch.getId()
                .equals(requestedBranch.getId())) {

            throw new AccessDeniedException(
                    "Bạn không được xem chi nhánh này"
            );
        }
    }

    private Branch requireUserBranch(User user) {

        if (user.getBranch() == null) {
            throw new AccessDeniedException(
                    "Tài khoản chưa được gán chi nhánh"
            );
        }

        return user.getBranch();
    }

    // ==================================================
    // KIỂM TRA ROLE
    // ==================================================

    private String getRoleName(User user) {

        if (user == null || user.getRole() == null) {
            return "";
        }

        String roleName = user.getRole().getName();

        return roleName == null
                ? ""
                : roleName.trim().toUpperCase();
    }

    private boolean isManager(User user) {
        return "MANAGER".equals(getRoleName(user));
    }

    private boolean canEdit(User user) {

        String role = getRoleName(user);

        return "MANAGER".equals(role)
                || "LEADER".equals(role);
    }

    // ==================================================
    // ĐẾM ĐÁNH GIÁ THEO SAO
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

    // ==================================================
    // CHUẨN HÓA MÃ CHI NHÁNH
    // ==================================================

    private String normalizeBranchCode(
            String branchCode
    ) {

        if (branchCode == null || branchCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Mã chi nhánh không hợp lệ"
            );
        }

        return branchCode
                .trim()
                .toUpperCase();
    }
}