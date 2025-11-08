package com.ucamp.project.dto;

import com.ucamp.project.model.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserDTO {
    private String userId; // 인증에서 userId를 FE에 바로 전달한다면 userId는 필요없음
    private String nickname;
    private String email;
    private Job job;
    private String passStatus;
    private String status;
    private String userProfileImageUrl;
    private String certStatus; // 합격자 신청 상태
}
