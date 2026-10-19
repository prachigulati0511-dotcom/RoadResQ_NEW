package com.roadresq.controller;

import com.roadresq.dto.ReviewDto;
import com.roadresq.model.User;
import com.roadresq.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> submitReview(@AuthenticationPrincipal User user,
                                          @Valid @RequestBody ReviewDto dto) {
        if (user == null) return ResponseEntity.status(401).build();
        try {
            ReviewDto saved = reviewService.submitReview(user, dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<List<ReviewDto>> getProviderReviews(@PathVariable Long providerId) {
        return ResponseEntity.ok(reviewService.getProviderReviews(providerId));
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<?> getReviewForRequest(@PathVariable Long requestId) {
        return reviewService.getReviewForRequest(requestId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}
