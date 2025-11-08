package com.ucamp.project.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "payment_seq_gen")
    @SequenceGenerator(
            name = "payment_seq_gen",
            sequenceName = "PAYMENT_SEQ",
            allocationSize = 1
    )
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "order_id", nullable = false, unique = true, length = 100)
    private String orderId;

    @Column(name = "payment_key", nullable = false, unique = true, length = 200)
    private String paymentKey;

    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;  // 결제 승인 시각

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;   // 구독 만료 시각

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;          // 결제 금액

    @Column(name = "payment_status", length = 10)
    @ColumnDefault("'ACTIVE'")
    private String paymentStatus;

    @Transient
    private Long userId;

    @PrePersist
    private void onCreate() {
        if (this.user == null && this.userId != null) {
            this.user = User.builder().userId(userId).build();
        }
        if (this.approvedAt == null) {
            this.approvedAt = LocalDateTime.now();
        }
        if (this.expiredAt == null) {
            // 기본 만료일: 1개월 후
            this.expiredAt = this.approvedAt.plusMonths(1);
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = "ACTIVE";
        }
    }
}
