package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class PostJobId implements Serializable {
    @ManyToOne
    @JoinColumn(name = "post_id",nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "job_id",nullable = false)
    private Job job;

    @Transient
    private Long jobId;
    @Transient
    private Long postId;

    @PrePersist
    private void onCreate() {
        if (this.job == null && this.jobId != null) {
            this.job = Job.builder().jobId(jobId).build();
        }
        if (this.post == null && this.postId != null) {
            this.post = Post.builder().postId(this.postId).build();
        }
    }
}
