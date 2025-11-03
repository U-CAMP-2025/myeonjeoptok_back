package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SimulationRecordItemDto {
    private Long simulationId;
    private String simulationStatus; // INPROGRESS / COMPLETED
    private PostBrief post;

    @Getter
    @Builder
    public static class PostBrief {
        private Long postId;
        private String title;
        private List<String> job;
    }
}