package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.dto.ReviewRequest;
import com.ucamp.project.dto.ReviewResponse;
import com.ucamp.project.model.Review;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.model.User;
import com.ucamp.project.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/posts/{postId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviewsByPostId(
            @PathVariable Long postId) {
        List<ReviewResponse> reviewList = reviewService.findReviewsByPostId(postId);
        return ResponseEntity.ok(reviewList);
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long postId,
            @RequestBody ReviewRequest reviewRequest,
            @AuthenticationPrincipal User user) {
        ReviewResponse createdReview = reviewService.createReview(postId, user.getUserId(), reviewRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long postId,
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user) {

        reviewService.deleteReview(postId, reviewId, user.getUserId());

        return ResponseEntity.noContent().build();
    }
}
