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
    public SseEmitter openSse(){
        // user에서 userId 추출
//        Long userId = user.getUserId();

        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        sseComponent.addEmitter(1L, emitter);

        //콜백함수 등록
        emitter.onCompletion(() -> {
            sseComponent.removeEmitter(1L);
        });

        emitter.onTimeout(() -> {
            sseComponent.removeEmitter(1L);
        });

        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (IOException e) {
            System.out.println("TESTETST!!!!");
            throw new RuntimeException(e);
        }
        return emitter;
    }

    @GetMapping
    public ApiResponse<Object> findAll(){
        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(notificationService.findAll(1L))
                .build();

        return resp;
    }

    @PutMapping("/{notiId}")
    public ResponseEntity<?> readOne(@PathVariable Long notiId){
        notificationService.readOne(notiId,1L);

        return ResponseEntity.status(204).build();
    }

    @PutMapping
    public ResponseEntity<?> readAll(){

        notificationService.readAll(1L);

        return ResponseEntity.status(204).build();
    }

    @DeleteMapping("/{notiId}")
    public ResponseEntity<?> deleteOne(@PathVariable Long notiId){

        notificationService.delOne(notiId,1L);

        return ResponseEntity.status(204).build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAll(){

        notificationService.delAll(1L);

        return ResponseEntity.status(204).build();

    }

}
