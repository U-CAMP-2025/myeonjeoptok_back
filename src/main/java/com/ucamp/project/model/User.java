package com.ucamp.project.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "USERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "kakao_id", length = 100, nullable = false)
    private String kakaoId;

    @Column(name = "nickname", length = 10, nullable = false, unique = true)
    private String nickname;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    /**
     * TODO: Job Entity 생기면 @ManyToOne로 바꿈
     */
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "status", length = 20)
    private String status; // e.g., ACTIVE, DISABLED, REGISTERING

    @Column(name = "pass_status", columnDefinition = "char(1)", length = 1)
    private String passStatus; // "Y" or "N"

    @Column(name = "created_at", nullable = false)
    private java.time.LocalDateTime createdAt;

    @Column(name = "role", length = 10)
    private String role; // USER, ADMIN, etc.

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
    }
}
