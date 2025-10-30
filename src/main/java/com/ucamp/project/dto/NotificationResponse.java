package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long notiId;
    private String content;
    private String type;
    private boolean read;
    private LocalDateTime createdAt;
}
