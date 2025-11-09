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
public class PostDetailDto {
    private Long postId;
    private String postTitle;
    private String postDescription;
    private Long bookmarkCount;
    private String postCreatedAt;
    private Long reviewCount;
    private List<JobDetailDto> jobs;
}
