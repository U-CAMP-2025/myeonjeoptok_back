package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "user_seq_gen")     // @SeqGen의 별명과 연결
    @SequenceGenerator(
            name = "user_seq_gen",      // generator과 연결할 별명 생성
            sequenceName = "USERS_SEQ", // DB에 생성한 시퀀스 이름과 연결
            allocationSize = 1          // 건너뜀 방지, 1개씩만 가져옴
    )
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "kakao_id", length = 100, nullable = false, unique = true)
    private String kakaoId;

    @Column(name = "nickname", length = 10, nullable = false, unique = true)
    private String nickname;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    /**
     * TODO: Job Entity 생기면 @ManyToOne로 바꿈
     */
    @ManyToOne
    @JoinColumn(name="job_id")
    private Job job;

    @Column(name = "status", length = 20)
    private String status; // e.g., ACTIVE, DISABLED, NEW

    @Column(name = "pass_status", columnDefinition = "char(1)", length = 1)
    private String passStatus; // "Y" or "N"

    @Column(name="users_profile_image_url", length = 255)
    private String usersProfileImageUrl;

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "role", length = 10)
    private String role; // USER, ADMIN, etc.

    @Transient
    private Long jobId;

    @Column
    private String refreshToken;

    @PrePersist
    private void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = java.time.LocalDateTime.now();
        }
        if (this.status == null || this.status.isBlank()) {
            this.status = "ACTIVE";
        }
        if (this.role == null || this.role.isBlank()) {
            this.role = "USER";
        }
        if (this.passStatus != null && !this.passStatus.isBlank()) {
            this.passStatus = this.passStatus.trim().toUpperCase().substring(0, 1);
        }
        if (this.jobId != null && this.job == null) {
            this.job = Job.builder().jobId(this.jobId).build();
        }
    }
}
