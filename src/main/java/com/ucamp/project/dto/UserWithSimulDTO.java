package com.ucamp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
public class UserWithSimulDTO {

    private String title;
    private String nickname;
    private String email;
    private String completedAt; // 날짜 타입을 String으로 가정 (DB에 따라 Date/LocalDateTime으로 변경 가능)
    private String status; // SUCCESS 또는 INPROGRESS

    // Native Query 결과를 매핑하기 위한 생성자
    public UserWithSimulDTO(String title, String nickname, String email, String completedAt, String status) {
        this.title = title;
        this.nickname = nickname;
        this.email = email;
        this.completedAt = completedAt;
        this.status = status;
    }
}
