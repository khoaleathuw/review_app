package com.example.review.repository;

import com.example.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Tất cả đánh giá mới nhất
    List<Review> findAllByOrderByCreatedAtDesc();

    // Đánh giá theo chi nhánh
    List<Review> findByBranch_CodeOrderByCreatedAtDesc(
            String branchCode
    );

    // Tổng số đánh giá
    long countByBranch_Code(
            String branchCode
    );

    // Đếm theo số sao
    long countByBranch_CodeAndRating(
            String branchCode,
            Integer rating
    );

    // Đếm từ 1 đến N sao
    long countByBranch_CodeAndRatingLessThanEqual(
            String branchCode,
            Integer rating
    );

     List<Review>
    findByBranch_IdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long branchId,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );
}