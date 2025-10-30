package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEW")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "review_content",nullable = false,length = 300)
    private String reviewContent;

    @Column(name = "review_created_at")
    private LocalDateTime reviewCreatedAt;


    @Transient
    private Long userId;
    @Transient
    private Long postId;

    @PrePersist
    private void onCreate() {
        if (this.user == null && this.userId != null) {
            this.user = User.builder().userId(userId).build();
        }
        if (this.post == null && this.postId != null) {
            this.post = Post.builder().postId(this.postId).build();
        }
        if (this.reviewCreatedAt == null) {
            this.reviewCreatedAt = LocalDateTime.now();
        }
    }

}
