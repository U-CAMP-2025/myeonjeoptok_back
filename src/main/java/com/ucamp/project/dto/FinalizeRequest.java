package com.ucamp.project.dto;

// com.ucamp.project.dto.FinalizeRequest.java
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinalizeRequest {
    private List<Item> qaList;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
        private Long qaId;          // 변경 대상 기존 QID
        private String transContent; // 바꿔 넣을 답변(300자 제한은 FE가 이미 체크)
    }
}
