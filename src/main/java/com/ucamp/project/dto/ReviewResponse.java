package com.ucamp.project.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String nickname;
    private String content;
    private LocalDateTime createdAt;
    private String profileImage;

    public static ReviewResponse fromEntity(Review review) {
        User user = review.getUser();

        return ReviewResponse.builder()
                .reviewId(review.getReviewId())
                .content(review.getReviewContent())
                .createdAt(review.getReviewCreatedAt())
                .nickname(user != null ? user.getNickname() : "유저없어요")
                .profileImage(user != null ? user.getUsersProfileImageUrl() : null)
                .build();
    }
}