package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimulationDetailResponse {
    private InterviewerDto interviewer;
    private PostDto post;
}