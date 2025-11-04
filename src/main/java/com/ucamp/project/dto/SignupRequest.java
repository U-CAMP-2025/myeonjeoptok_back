package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Data;
//

@Data
@Builder
public class SignupRequest {
    private String nickname;
    private String email;
    private Long jobId;
    private String kakaoId;
}
