package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.service.NotificationService;
import com.ucamp.project.sse.SseComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final SseComponent sseComponent;
    private final NotificationService notificationService;

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter openSse(){

        Long userId = 101l;

        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);

        sseComponent.addEmitter(userId, emitter);

        //콜백함수 등록
        emitter.onCompletion(() -> {
            sseComponent.removeEmitter(userId);
        });

        emitter.onTimeout(() -> {
            sseComponent.removeEmitter(userId);
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
                .data(notificationService.findAll(101l))
                .build();

        return resp;
    }

    @PutMapping("/{notiId}")
    public ResponseEntity<?> readOne(@PathVariable Long notiId){

        notificationService.readOne(notiId,101l);

        return ResponseEntity.status(204).build();
    }

    @PutMapping
    public ResponseEntity<?> readAll(){

        notificationService.readAll(101l);

        return ResponseEntity.status(204).build();
    }

    @DeleteMapping("/{notiId}")
    public ResponseEntity<?> deleteOne(@PathVariable Long notiId){

        notificationService.delOne(notiId, 101l);

        return ResponseEntity.status(204).build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAll(){

        notificationService.delAll(101l);

        return ResponseEntity.status(204).build();

    }

}
