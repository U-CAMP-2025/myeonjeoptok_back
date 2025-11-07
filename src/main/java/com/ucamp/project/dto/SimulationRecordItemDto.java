package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class SimulationRecordItemDto {
    private Long simulationId;
    private String simulationStatus; // INPROGRESS / COMPLETED
    private PostBrief post;
    private Long count;
    private LocalDateTime completedAt;

    private Integer repetitionCount;    // 같은 post로 몇 번 했는지
    private LocalDateTime latestCompletedAt; // 최신 완료 일시
    private Long latestSimulationId;    // 최신 완료 simId (없으면 최신 simId)


    @Getter
    @Setter
    @Builder
    public static class PostBrief {
        private Long postId;
        private String title;
        private List<String> job;
    }
}