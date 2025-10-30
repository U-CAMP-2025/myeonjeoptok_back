package com.ucamp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserDTO {
    private String nickname;
    private String email;
    private String job;
    private String passStatus;
    private String status;
    private String userProfileImageUrl;
}
