package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobDetailDto {
    private Long jobId;
    private String jobName;
}
