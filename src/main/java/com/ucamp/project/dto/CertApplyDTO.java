package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CertApplyDTO {
    private Long userId;
    private String fileUrl;
}
