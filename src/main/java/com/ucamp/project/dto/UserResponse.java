package com.ucamp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String nickname;
    private String email;
    private Long jobId;
    private String jobName;
    private String passStatus;
    private LocalDateTime createdAt;
    private String role;
    private String simulationStatus;
    private LocalDateTime simulationCompletedAt;
    private String certStatus;
    private LocalDateTime certReqDate;
    private LocalDateTime certTrmtDate;


    // 관리자 페이지에서 유저 전체 조회용 생성자
    public UserResponse(String nickname, String email, Long jobId, String jobName,
                        String passStatus, LocalDateTime createdAt, String role,
                        String simulationStatus, LocalDateTime simulationCompletedAt,
                        String certStatus, LocalDateTime certReqDate, LocalDateTime certTrmtDate) {
        this.nickname = nickname;
        this.email = email;
        this.jobId = jobId;
        this.jobName = jobName;
        this.passStatus = passStatus;
        this.createdAt = createdAt;
        this.role = role;
        this.simulationStatus = simulationStatus;
        this.simulationCompletedAt = simulationCompletedAt;
        this.certStatus = certStatus;
        this.certReqDate = certReqDate;
        this.certTrmtDate = certTrmtDate;
    }
}
