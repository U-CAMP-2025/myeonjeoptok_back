package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InterviewerDto {
    private Long interviewerId;
    private String interviewerImageUrl;
    private String name; // 있으면
}