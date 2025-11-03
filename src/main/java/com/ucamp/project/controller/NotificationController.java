package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.model.User;
import com.ucamp.project.service.NotificationService;
import com.ucamp.project.sse.SseComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Slf4j
public class NotificationController {
    private final SseComponent sseComponent;
    private final NotificationService notificationService;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter openSse(@AuthenticationPrincipal User user){

        log.info("TTEST " + user);
        // user에서 userId 추출
        Long userId = user.getUserId();

        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        //콜백함수 등록
        emitter.onCompletion(() -> {
            sseComponent.removeEmitter(userId);
        });

        emitter.onTimeout(() -> {
            sseComponent.removeEmitter(userId);
        });

        sseComponent.addEmitter(userId, emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            throw new RuntimeException("SSE 연결 실패");
        }
        return emitter;
    }

    @GetMapping
    public ApiResponse<Object> findAll(@AuthenticationPrincipal User user){
        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(notificationService.findAll(user.getUserId()))
                .build();

        return resp;
    }

    @PutMapping("/{notiId}")
    public ResponseEntity<?> readOne(@PathVariable Long notiId, @AuthenticationPrincipal User user){
        notificationService.readOne(notiId,user.getUserId());

        return ResponseEntity.status(204).build();
    }

    @PutMapping
    public ResponseEntity<?> readAll(@AuthenticationPrincipal User user){

        notificationService.readAll(user.getUserId());

        return ResponseEntity.status(204).build();
    }

    @DeleteMapping("/{notiId}")
    public ResponseEntity<?> deleteOne(@PathVariable Long notiId,@AuthenticationPrincipal User user){

        notificationService.delOne(notiId,user.getUserId());

        return ResponseEntity.status(204).build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAll(@AuthenticationPrincipal User user){

        notificationService.delAll(user.getUserId());

        return ResponseEntity.status(204).build();

    }

}
