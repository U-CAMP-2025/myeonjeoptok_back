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
public class PostCreateRequestDTO {

    private Long postId;
    private List<Long> jobIds;
    private String title;
    private String summary;
    private List<QaSet> qaSets;
    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QaSet {
        private Long qaId;
        private String question;
        private String answer;
    }
}
