package com.example.review.repository;

import com.example.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Lấy toàn bộ đánh giá mới nhất
    List<Review> findAllByOrderByCreatedAtDesc();

    // Lấy đánh giá của một chi nhánh
    List<Review> findByBranchCodeOrderByCreatedAtDesc(
            String branchCode
    );

    // Tổng số đánh giá của chi nhánh
    long countByBranchCode(
            String branchCode
    );

    // Đếm chính xác theo từng mức sao
    long countByBranchCodeAndRating(
            String branchCode,
            Integer rating
    );

    // Đếm đánh giá thấp, ví dụ từ 1 đến 3 sao
    long countByBranchCodeAndRatingLessThanEqual(
            String branchCode,
            Integer rating
    );
}