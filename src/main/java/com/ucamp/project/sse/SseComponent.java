package com.ucamp.project.sse;

import com.ucamp.project.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Component
@Slf4j
public class SseComponent {

    @Autowired
    private NotificationService notificationService;

    // 빈으로 등록된 SseEmitter 리스트
    private final Map<Long,SseEmitter> emitters = new HashMap<>();

    public SseEmitter getEmitters(Long userId) {
        return emitters.get(userId);
    }

    // SseEmitter 추가
    public void addEmitter(Long userId, SseEmitter emitter) {
        emitters.put(userId,emitter);
    }

    // SseEmitter 제거
    public void removeEmitter(Long userId) {
        emitters.remove(userId);
    }

    public boolean eventtrigger(Long userId) {
        SseEmitter emitter = emitters.get(userId);
        if(emitter == null){
            return false;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name("message")
                    .data(true));
            return true;
        } catch (IOException e) {
            log.info("SSE 연결된 사용자 없음");
        }
        return false;
    }
}