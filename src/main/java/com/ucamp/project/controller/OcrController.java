package com.ucamp.project.controller;

import com.ucamp.project.service.OcrOpenService;
import com.ucamp.project.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;
    private final OcrOpenService ocrOpenService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> detectText(@RequestBody Map<String, String> body) throws Exception {
        String imageUrl = body.get("imageUrl");
        Map<String, Object> result = ocrOpenService.detectText(imageUrl);
        return ResponseEntity.ok(result);
    }
}
