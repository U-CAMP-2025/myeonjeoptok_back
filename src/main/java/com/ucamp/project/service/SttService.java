// SttService.java
package com.ucamp.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Path;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SttService {

    @Value("${openai.apiKey}")
    private String apiKey;

    // 최신 권장 STT 모델명 (Whisper 대체)
    @Value("${OPENAI_STT_MODEL}")
    private String sttModel;

    @Value("${openai.sttPrompt:}")
    private String sttPrompt;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + System.getenv("OPENAI_API_KEY"))
            .build();

    public String transcribe(Path audioPath) {
        try {
            // 반드시 MultipartBodyBuilder 사용
            org.springframework.util.MultiValueMap<String, HttpEntity<?>> body = buildMultipart(audioPath);

            return webClient.post()
                    .uri("/v1/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    // 에러 바디를 그대로 받아 디버깅
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class).map(msg ->
                                    new RuntimeException("OpenAI STT error " + resp.statusCode() + " :: " + msg))
                    )
                    .bodyToMono(Map.class)
                    .map(map -> String.valueOf(map.getOrDefault("text", "")))
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("STT 호출 실패: " + e.getMessage(), e);
        }
    }

    private MultiValueMap<String, HttpEntity<?>> buildMultipart(Path audioPath) {
        var builder = new org.springframework.http.client.MultipartBodyBuilder();

        // 1) model
        builder.part("model", sttModel);

        // 2) prompt(옵션)
        if (sttPrompt != null && !sttPrompt.isBlank()) {
            builder.part("prompt", sttPrompt);
        }

        // 3) language(옵션) — ko/en 혼용이면 생략 가능. 필요 시 아래 주석 해제
        // builder.part("language", "ko");

        // 4) file — 파일명과 content-type이 함께 가도록 FileSystemResource 사용
        FileSystemResource fs = new FileSystemResource(audioPath.toFile());
        ContentDisposition cd = ContentDisposition
                .formData()
                .name("file")
                .filename(fs.getFilename() != null ? fs.getFilename() : "audio.webm")
                .build();

        HttpHeaders fh = new HttpHeaders();
        fh.setContentDisposition(cd);
        // webm이면 audio/webm, wav면 audio/wav 등 적절히
        fh.setContentType(MediaType.parseMediaType("audio/webm"));

        HttpEntity<FileSystemResource> fileEntity = new HttpEntity<>(fs, fh);
        builder.part("file", fileEntity);

        return builder.build();
    }
}
