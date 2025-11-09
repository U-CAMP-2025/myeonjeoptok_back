package com.ucamp.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class UserDetailResponse {
    private Long userId;
    private String nickname;
    private String userImageUrl;
    private String email;
    private Character passStatus;
    private String jobName;
    private String paymentStatus;
    private List<PostDetailDto> posts;

}
