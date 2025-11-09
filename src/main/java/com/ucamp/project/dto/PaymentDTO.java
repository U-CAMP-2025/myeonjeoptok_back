package com.ucamp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private String orderId;
    private String paymentKey;
    private LocalDateTime approvedAt;
    private LocalDateTime startedAt;
    private LocalDateTime expiredAt;
    private Long totalAmount;
    private String paymentStatus;
}
