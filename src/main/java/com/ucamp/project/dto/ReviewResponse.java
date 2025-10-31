package com.ucamp.project.dto;

import com.ucamp.project.model.Review;
import com.ucamp.project.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {
    private Long reviewId;
    private String reviewContent;
    private LocalDateTime reviewCreatedAt;
    private Long userId;
    private String username; // 예시

    public static ReviewResponse fromEntity(Review review) {
        User user = review.getUser();

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .reviewContent(review.getReviewContent())
                .reviewCreatedAt(review.getReviewCreatedAt())
                .userId(user != null ? user.getUserId() : null)
                .username(user != null ? user.getNickname() : "유저없어요")
                .build();
    }
}