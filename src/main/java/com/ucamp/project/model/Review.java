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
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "review_seq_gen")
    @SequenceGenerator(
            name = "review_seq_gen",
            sequenceName = "REVIEW_SEQ",
            allocationSize = 1
    )
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


}
