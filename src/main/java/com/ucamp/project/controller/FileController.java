package com.ucamp.project.controller;

import com.nimbusds.jose.util.Pair;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Controller
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
    public ResponseEntity<Map<String, String>> uploadFile(
            @PathVariable String type,
            @RequestPart("file") MultipartFile file) {

        String fileName = fileService.saveFile(type, file);
        return ResponseEntity.ok(Map.of("fileName", fileName));
    }
}
