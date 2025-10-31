package com.ucamp.project.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

@Component
public class SseComponent {
    //
    // 빈으로 등록된 SseEmitter 리스트
    private final Map<Long,SseEmitter> emitters = new HashMap<>();

    public SseEmitter getEmitters(Long userId) {

        for(Long sseId : emitters.keySet()) {
            System.out.println("TEST : " + (sseId==userId));
            System.out.println("TEST : " + sseId);
            System.out.println("TEST : " + userId);
            if (Objects.equals(sseId, userId)) {
                return emitters.get(sseId);
            }
        }
        return null;
    }

    // SseEmitter 추가
    public void addEmitter(Long userId, SseEmitter emitter) {
        emitters.put(userId,emitter);
    }

    // SseEmitter 제거
    public void removeEmitter(Long userId) {
        emitters.remove(userId);
    }
}