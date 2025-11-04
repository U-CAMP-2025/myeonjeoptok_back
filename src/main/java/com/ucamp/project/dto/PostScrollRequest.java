package com.ucamp.project.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class PostScrollRequest {
        private int page;            // 페이지 번호
        private int limit;           // 한 페이지 아이템 수
        private String sort;         // 정렬 기준
        private List<Long> jobs;  // 선택된 직무 ID 배열
}
