package com.ucamp.project.service;

import com.ucamp.project.dto.ReviewRequest;
import com.ucamp.project.dto.ReviewResponse;
import com.ucamp.project.model.Notification;
import com.ucamp.project.model.Post;
import com.ucamp.project.model.Review;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.NotificationRepository;
import com.ucamp.project.repository.PostRepository;
import com.ucamp.project.repository.ReviewRepository;
import com.ucamp.project.repository.UserRepository;
import com.ucamp.project.sse.SseComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final SseComponent sseComponent;
    private final NotificationRepository notificationRepository;

    public List<ReviewResponse> findReviewsByPostId(Long postId) {
        List<Review> reviewEntityList = reviewRepository.findByPost_PostId(postId);

        List<ReviewResponse> reviewDtoList = reviewEntityList.stream()
                .map(ReviewResponse::fromEntity)
                .collect(Collectors.toList());
        return reviewDtoList;
    }

    @Transactional
    public ReviewResponse createReview(Long postId, Long userId, ReviewRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("포스트 찾을 수 없음 / id = " + postId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 찾을 수 없음 / id = " + userId));

        Review newReview = Review.builder()
                .post(post)
                .user(user)
                .reviewContent(request.getContent())
                .build();

        Review reviewSaved = reviewRepository.save(newReview);

        //자기자신 알람 방지
        if(!userId.equals(post.getUser().getUserId())){
            String message = post.getPostTitle()+":::"+postId;

            Notification noti = Notification.builder()
                    .notiId(null)
                    .notiContent(message)
                    .user(post.getUser())
                    .notiType("REVIEW")
                    .notiRead("N")
                    .build();

            notificationRepository.save(noti);

            sseComponent.eventtrigger(post.getUser().getUserId());
        }
        return ReviewResponse.fromEntity(reviewSaved);
    }

    public void deleteReview(Long postId, Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 찾을 수 없음 / id = " + reviewId));

        if (!review.getPost().getPostId().equals(postId)) {
            throw new IllegalArgumentException("포스트 불일치");
        }

        if (!review.getUser().getUserId().equals(userId)) {
            throw new IllegalStateException("삭제 권한 없음");
        }

        reviewRepository.delete(review);
    }

    public Object save(Review review) {
        return reviewRepository.save(review);
    }
}
