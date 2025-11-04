package com.ucamp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CustomUserDetails {
    // TODO: JWT 토큰 적용 전 임시 클래스
    private Long userId = 1L;
}
