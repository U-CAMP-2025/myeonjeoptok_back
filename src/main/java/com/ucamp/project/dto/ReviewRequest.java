package com.ucamp.project.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ReviewRequest {
    private String reviewContent;
}

