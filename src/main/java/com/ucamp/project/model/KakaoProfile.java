package com.ucamp.project.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class KakaoProfile {
    private Long id;
    private String email;
    private String profileImageUrl;
}