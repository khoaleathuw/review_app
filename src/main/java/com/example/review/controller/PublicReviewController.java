package com.example.review.controller;

import com.example.review.dto.CreateReviewRequest;
import com.example.review.entity.Review;
import com.example.review.service.ReviewService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/reviews")
@CrossOrigin("*")
public class PublicReviewController {

    private final ReviewService reviewService;

    public PublicReviewController(
            ReviewService reviewService
    ) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestBody CreateReviewRequest request
    ) {
        try {

            Review review =
                    reviewService.createReview(request);

            Map<String, Object> response =
                    new LinkedHashMap<>();

            response.put("success", true);
            response.put(
                    "message",
                    "Cảm ơn bạn đã đánh giá"
            );
            response.put(
                    "reviewId",
                    review.getId()
            );
            response.put(
                    "rating",
                    review.getRating()
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", e.getMessage()
                            )
                    );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "success", false,
                                    "message", e.getMessage()
                            )
                    );
        }
    }
}