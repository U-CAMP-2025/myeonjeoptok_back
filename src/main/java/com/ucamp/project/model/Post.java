package com.ucamp.project.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "POST")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "post_title", nullable = false, length = 30)
    private String postTitle;

    @Column(name = "post_description", length = 100)
    private String postDescription;

    @Column(name = "post_import_count",nullable = false)
    @ColumnDefault("0")
    private Long count;

    @Column(name = "post_created_at",  nullable = false)
    private LocalDateTime postCreatedAt;

    @Column(name = "post_update_at")
    private LocalDateTime postUpdatedAt;

    @Column(name = "post_status",length = 1)
    @ColumnDefault("'Y'")
    private String postStatus;

    @ManyToOne
    @JoinColumn(name = "post_other_writer")
    private User postOtherWriter;

    @Transient
    private Long userId;

    @Transient
    private Long postOtherWriterId;

    @PrePersist
    private void onCreate() {
        if (this.user == null && this.userId != null) {
            this.user = User.builder().userId(userId).build();
        }
        if (this.postOtherWriter == null && this.postOtherWriterId != null) {
            this.postOtherWriter = User.builder().userId(postOtherWriterId).build();
        }
        if (this.postCreatedAt == null) {
            this.postCreatedAt = LocalDateTime.now();
        }
    }



}
