package com.ucamp.project.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimulationResultDto {
    private Long simulationId;
    private PostDto post;
}
