package com.ucamp.project.controller;

import com.nimbusds.jose.util.Pair;
import com.ucamp.project.model.Certificate;
import com.ucamp.project.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileService fileService;

    @GetMapping("/image/{type}/{id}")
    public ResponseEntity<?> image(@PathVariable String type, @PathVariable String id) {

        Pair<String,Resource> pair = fileService.getImg(type, id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(pair.getLeft()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + pair.getRight().getFilename() + "\"")
                .body(pair.getRight());

    }

    // 파일 업로드
    @PostMapping("/api/files/{type}")
    public ResponseEntity<Map<String, Serializable>> uploadFile(
            @PathVariable String type,
            @RequestPart("file") MultipartFile file) {
        log.info("호출됨??????");
        log.info("파일이름???: {}",String.valueOf(file));
        // 파일과 cert_id를 함께 처리하는 통합 로직 호출
        log.info("파일 업로드 요청, type = {}", type);
        String fileName = fileService.saveTempFile(type, file); // 파일명 반환

        return ResponseEntity.ok(Map.of("fileName", fileName));
    }
}
