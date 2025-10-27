package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private String nickname;
    private String email;
}
