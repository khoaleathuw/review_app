package com.example.review.service;

import com.example.review.dto.CreateReviewRequest;
import com.example.review.entity.Branch;
import com.example.review.entity.Review;
import com.example.review.entity.User;
import com.example.review.repository.BranchRepository;
import com.example.review.repository.ReviewRepository;
import com.example.review.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BranchRepository branchRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
    }

    // ==================================================
    // TẠO ĐÁNH GIÁ
    // ==================================================

    @Transactional
    public Review createReview(
            CreateReviewRequest request
    ) {

        validateRequest(request);

        String branchCode =
                normalizeBranchCode(
                        request.getBranchCode()
                );

        Branch branch = branchRepository
                .findByCodeIgnoreCaseAndActiveTrue(
                        branchCode
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy chi nhánh "
                                        + branchCode
                                        + " hoặc chi nhánh đã bị khóa"
                        )
                );

        Review review = new Review();

        review.setBranch(branch);
        review.setRating(request.getRating());
        review.setReason(
                clean(request.getReason())
        );
        review.setComment(
                clean(request.getComment())
        );
        review.setDeviceName(
                clean(request.getDeviceName())
        );

        return reviewRepository.save(review);
    }

    // ==================================================
    // LẤY TẤT CẢ ĐÁNH GIÁ
    // ==================================================

    @Transactional(readOnly = true)
    public List<Review> getAllReviews() {

        return reviewRepository
                .findAllByOrderByCreatedAtDesc();
    }

    // ==================================================
    // LẤY ĐÁNH GIÁ THEO ID
    // ==================================================

    @Transactional(readOnly = true)
    public Review getById(
            Long id
    ) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "ID đánh giá không hợp lệ"
            );
        }

        return reviewRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Không tìm thấy đánh giá với ID: "
                                        + id
                        )
                );
    }

    // ==================================================
    // LẤY ĐÁNH GIÁ THEO ID VÀ KIỂM TRA QUYỀN
    // ==================================================

    @Transactional(readOnly = true)
    public Review getByIdForManagement(
            Long id,
            Authentication authentication
    ) {

        Review review = getById(id);

        checkManagementPermission(
                review,
                authentication
        );

        return review;
    }

    // ==================================================
    // LẤY ĐÁNH GIÁ THEO MÃ CHI NHÁNH
    // ==================================================

    @Transactional(readOnly = true)
    public List<Review> getReviewsByBranch(
            String branchCode
    ) {

        String normalizedCode =
                normalizeBranchCode(branchCode);

        return reviewRepository
                .findByBranch_CodeOrderByCreatedAtDesc(
                        normalizedCode
                );
    }

    // ==================================================
    // LẤY ĐÁNH GIÁ THEO CHI NHÁNH VÀ KHOẢNG THỜI GIAN
    // ==================================================

    @Transactional(readOnly = true)
    public List<Review> getReviewsByBranchAndDateRange(
            Long branchId,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {

        if (branchId == null) {
            throw new IllegalArgumentException(
                    "ID chi nhánh không hợp lệ"
            );
        }

        if (
                fromDate == null
                        || toDate == null
        ) {
            throw new IllegalArgumentException(
                    "Khoảng thời gian không hợp lệ"
            );
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được sau ngày kết thúc"
            );
        }

        return reviewRepository
                .findByBranch_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        branchId,
                        fromDate,
                        toDate
                );
    }

    // ==================================================
    // TỔNG SỐ ĐÁNH GIÁ CỦA CHI NHÁNH
    // ==================================================

    @Transactional(readOnly = true)
    public long countReviewsByBranch(
            String branchCode
    ) {

        String normalizedCode =
                normalizeBranchCode(branchCode);

        return reviewRepository
                .countByBranch_Code(
                        normalizedCode
                );
    }

    // ==================================================
    // ĐẾM ĐÁNH GIÁ THEO SỐ SAO
    // ==================================================

    @Transactional(readOnly = true)
    public long countByRating(
            String branchCode,
            Integer rating
    ) {

        String normalizedCode =
                normalizeBranchCode(branchCode);

        validateRating(rating);

        return reviewRepository
                .countByBranch_CodeAndRating(
                        normalizedCode,
                        rating
                );
    }

    // ==================================================
    // ĐẾM ĐÁNH GIÁ THẤP 1–3 SAO
    // ==================================================

    @Transactional(readOnly = true)
    public long countLowRatings(
            String branchCode
    ) {

        String normalizedCode =
                normalizeBranchCode(branchCode);

        return reviewRepository
                .countByBranch_CodeAndRatingLessThanEqual(
                        normalizedCode,
                        3
                );
    }

    // ==================================================
    // SỬA ĐÁNH GIÁ
    // MANAGER: sửa mọi chi nhánh
    // LEADER: chỉ sửa chi nhánh của mình
    // EMPLOYEE: không được sửa
    // ==================================================

    @Transactional
    public Review updateReview(
            Long id,
            Integer rating,
            String reason,
            String comment,
            Authentication authentication
    ) {

        Review review = getById(id);

        checkManagementPermission(
                review,
                authentication
        );

        validateRating(rating);

        String cleanedReason =
                clean(reason);

        String cleanedComment =
                clean(comment);

        if (
                rating <= 3
                        && cleanedReason == null
        ) {
            throw new IllegalArgumentException(
                    "Đánh giá từ 1 đến 3 sao cần có lý do"
            );
        }

        if (
                cleanedReason != null
                        && cleanedReason.length() > 255
        ) {
            throw new IllegalArgumentException(
                    "Lý do tối đa 255 ký tự"
            );
        }

        if (
                cleanedComment != null
                        && cleanedComment.length() > 2000
        ) {
            throw new IllegalArgumentException(
                    "Nội dung đánh giá tối đa 2000 ký tự"
            );
        }

        review.setRating(rating);
        review.setReason(cleanedReason);
        review.setComment(cleanedComment);

        return reviewRepository.save(review);
    }

    // ==================================================
    // XÓA ĐÁNH GIÁ
    // Trả về ID chi nhánh để Controller redirect
    // ==================================================

    @Transactional
    public Long deleteReview(
            Long id,
            Authentication authentication
    ) {

        Review review = getById(id);

        checkManagementPermission(
                review,
                authentication
        );

        if (review.getBranch() == null) {
            throw new IllegalArgumentException(
                    "Đánh giá không thuộc chi nhánh nào"
            );
        }

        Long branchId =
                review.getBranch().getId();

        if (branchId == null) {
            throw new IllegalArgumentException(
                    "ID chi nhánh không hợp lệ"
            );
        }

        reviewRepository.delete(review);

        return branchId;
    }

    // ==================================================
    // LẤY NGƯỜI DÙNG ĐANG ĐĂNG NHẬP
    // ==================================================

    private User getCurrentUser(
            Authentication authentication
    ) {

        if (
                authentication == null
                        || !authentication.isAuthenticated()
                        || authentication.getName() == null
        ) {
            throw new SecurityException(
                    "Bạn chưa đăng nhập"
            );
        }

        return userRepository
                .findByUsername(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new SecurityException(
                                "Không tìm thấy tài khoản đang đăng nhập"
                        )
                );
    }

    // ==================================================
    // KIỂM TRA QUYỀN SỬA/XÓA
    // ==================================================

    private void checkManagementPermission(
            Review review,
            Authentication authentication
    ) {

        User currentUser =
                getCurrentUser(authentication);

        if (
                currentUser.getRole() == null
                        || currentUser.getRole().getName() == null
        ) {
            throw new SecurityException(
                    "Tài khoản chưa được cấp vai trò"
            );
        }

        String roleName =
                currentUser
                        .getRole()
                        .getName()
                        .trim()
                        .toUpperCase();

        // Manager được quản lý tất cả đánh giá
        if ("MANAGER".equals(roleName)) {
            return;
        }

        // Leader chỉ được quản lý chi nhánh của mình
        if ("LEADER".equals(roleName)) {

            if (currentUser.getBranch() == null) {
                throw new SecurityException(
                        "Tài khoản Leader chưa được gán chi nhánh"
                );
            }

            if (review.getBranch() == null) {
                throw new SecurityException(
                        "Đánh giá không thuộc chi nhánh hợp lệ"
                );
            }

            Long leaderBranchId =
                    currentUser
                            .getBranch()
                            .getId();

            Long reviewBranchId =
                    review
                            .getBranch()
                            .getId();

            if (
                    leaderBranchId == null
                            || reviewBranchId == null
                            || !leaderBranchId.equals(
                                    reviewBranchId
                            )
            ) {
                throw new SecurityException(
                        "Leader chỉ được sửa hoặc xóa đánh giá của chi nhánh mình"
                );
            }

            return;
        }

        throw new SecurityException(
                "Bạn không có quyền sửa hoặc xóa đánh giá"
        );
    }

    // ==================================================
    // KIỂM TRA DỮ LIỆU TẠO ĐÁNH GIÁ
    // ==================================================

    private void validateRequest(
            CreateReviewRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu đánh giá không được để trống"
            );
        }

        if (
                request.getBranchCode() == null
                        || request.getBranchCode().isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Thiếu mã chi nhánh"
            );
        }

        Integer rating =
                request.getRating();

        validateRating(rating);

        if (
                rating <= 3
                        && (
                        request.getReason() == null
                                || request.getReason().isBlank()
                )
        ) {
            throw new IllegalArgumentException(
                    "Đánh giá từ 1 đến 3 sao cần chọn lý do"
            );
        }

        String reason =
                clean(request.getReason());

        String comment =
                clean(request.getComment());

        String deviceName =
                clean(request.getDeviceName());

        if (
                reason != null
                        && reason.length() > 255
        ) {
            throw new IllegalArgumentException(
                    "Lý do tối đa 255 ký tự"
            );
        }

        if (
                comment != null
                        && comment.length() > 2000
        ) {
            throw new IllegalArgumentException(
                    "Nội dung đánh giá tối đa 2000 ký tự"
            );
        }

        if (
                deviceName != null
                        && deviceName.length() > 255
        ) {
            throw new IllegalArgumentException(
                    "Tên thiết bị tối đa 255 ký tự"
            );
        }
    }

    // ==================================================
    // KIỂM TRA SỐ SAO
    // ==================================================

    private void validateRating(
            Integer rating
    ) {

        if (
                rating == null
                        || rating < 1
                        || rating > 5
        ) {
            throw new IllegalArgumentException(
                    "Số sao phải từ 1 đến 5"
            );
        }
    }

    // ==================================================
    // CHUẨN HÓA MÃ CHI NHÁNH
    // ==================================================

    private String normalizeBranchCode(
            String branchCode
    ) {

        if (
                branchCode == null
                        || branchCode.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Mã chi nhánh không hợp lệ"
            );
        }

        return branchCode
                .trim()
                .toUpperCase();
    }

    // ==================================================
    // LÀM SẠCH CHUỖI
    // ==================================================

    private String clean(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}