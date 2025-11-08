package com.ucamp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailDto {
    private Long userId;
    private String nickname;
    private String email;
    private String usersProfileImageUrl;
    private Character passStatus;
    private String jobName;
}
