package com.ucamp.project.dto;

import com.ucamp.project.model.Qa;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PostResultDto {
    private Long postId;
    private String postTitle;
    private String postDescription;
    private List<QaDto> qaList;
}