package com.ucamp.project.dto;

import lombok.*;
import oracle.sql.CHAR;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RankResponse {
    private Long userId;
    private String nickname;
    private Character passStatus;
    private String profileImageUrl;
    private String jobName;
    private Long cnt;
}
