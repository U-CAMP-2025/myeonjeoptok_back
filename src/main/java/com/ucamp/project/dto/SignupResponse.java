package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignupResponse {
    private Long userId;
    private String nickname;
    private String email;
    private Long jobId;
    private String kakaoId;
}
