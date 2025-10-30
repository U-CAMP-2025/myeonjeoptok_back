package com.ucamp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimualtionPostResponse {
    private Long postId;
    private String title;
    private List<String> job;
}
