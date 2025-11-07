package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QaDto {
    private Long qaId;
    private Long qaOrder;
    private String qaQuestion;
    private String qaAnswer;
    private String transContent;
    private String feedback;
}