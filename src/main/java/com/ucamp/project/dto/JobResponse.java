package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobResponse {
    private Long jobId;
    private String jobName;
}
