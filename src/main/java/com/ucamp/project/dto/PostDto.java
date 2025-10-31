package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostDto {
    private Long postId;
    private String postTitle;
    private String postDescription;
    private List<QaDto> qaList;
}