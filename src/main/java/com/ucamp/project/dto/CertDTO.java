package com.ucamp.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CertDTO {
    private String certFileUrl;
    private LocalDateTime certTrmtDate;
    private String certStatus;
}
