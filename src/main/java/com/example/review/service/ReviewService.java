package com.example.review.service;

import com.example.review.dto.CreateReviewRequest;
import com.example.review.entity.Branch;
import com.example.review.entity.Review;
import com.example.review.repository.BranchRepository;
import com.example.review.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BranchRepository branchRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BranchRepository branchRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.branchRepository = branchRepository;
    }

    @Transactional
    public Review createReview(CreateReviewRequest request) {

        validateRequest(request);

        Branch branch = branchRepository
                .findByCode(request.getBranchCode())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy chi nhánh: "
                                        + request.getBranchCode()
                        )
                );

        if (!Boolean.TRUE.equals(branch.getActive())) {
            throw new RuntimeException(
                    "Chi nhánh hiện đang ngừng hoạt động"
            );
        }

        Review review = new Review();

        review.setBranch(branch);
        review.setRating(request.getRating());
        review.setReason(clean(request.getReason()));
        review.setComment(clean(request.getComment()));
        review.setDeviceName(clean(request.getDeviceName()));

        return reviewRepository.save(review);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Review> getReviewsByBranch(String branchCode) {
        return reviewRepository
                .findByBranchCodeOrderByCreatedAtDesc(branchCode);
    }

    private void validateRequest(CreateReviewRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu đánh giá không được để trống"
            );
        }

        if (request.getBranchCode() == null
                || request.getBranchCode().isBlank()) {
            throw new IllegalArgumentException(
                    "Thiếu mã chi nhánh"
            );
        }

        if (request.getRating() == null
                || request.getRating() < 1
                || request.getRating() > 5) {
            throw new IllegalArgumentException(
                    "Số sao phải từ 1 đến 5"
            );
        }

        if (request.getRating() <= 3
                && (request.getReason() == null
                || request.getReason().isBlank())) {
            throw new IllegalArgumentException(
                    "Đánh giá từ 1 đến 3 sao cần chọn lý do"
            );
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}