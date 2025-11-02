package com.ucamp.project.dto;

import com.ucamp.project.model.Qa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponseDTO {
    private Long postId;
    private String nickname;       // 내 닉네임
    private String otherWriter;    // 내가 만든 글이면 null
    private List<String> job;      // 직무 배열
    private List<Long> jobIds;      // 직무 배열
    private String title;          // 글 제목
    private String description;    // 글 설명
    private Long bookCount;         // 담은 사람 수
    private int review;            // 리뷰 수
    private LocalDateTime createAt;
    private boolean isPassed;
    private boolean isPublic;
    private boolean isMe;
    private List<PostCreateRequestDTO.QaSet> qa;

}