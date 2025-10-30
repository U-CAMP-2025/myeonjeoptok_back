package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_id")
    private Long notiId;

    @Column(name = "noti_content", length = 100)
    private String notiContent;

    @Column(name = "noti_type",length = 10)
    private String notiType;

    @Column(name = "noti_read",length = 1)
    @ColumnDefault("'Y'")
    private String notiRead;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "noti_created_at")
    private LocalDateTime notiCreatedAt;

    @Transient
    private Long userId;


    @PrePersist
    private void onCreate() {
        if (this.user == null && this.userId != null) {
            this.user = User.builder().userId(userId).build();
        }
        if (this.notiCreatedAt == null) {
            this.notiCreatedAt = LocalDateTime.now();
        }
    }
}
