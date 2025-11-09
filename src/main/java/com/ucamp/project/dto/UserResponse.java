package com.ucamp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long userId;
    private String nickname;
    private String email;
    private Long jobId;
    private String jobName;
    private String passStatus;
    private LocalDateTime createdAt;
    private String role;
    private LocalDateTime simulationCompletedAt;
    private String certStatus;
    private LocalDateTime certReqDate;
    private LocalDateTime certTrmtDate;
    private String certFileUrl;
    private String paymentStatus;

}
