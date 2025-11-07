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

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SttService {

    @Value("${openai.apiKey}")
    private String apiKey;

    @Value("${openai.baseUrl}")
    private String baseUrl; // ex) https://api.openai.com/v1

    @Value("${openai.sttModel}")
    private String sttModel;

    @Value("${openai.sttPrompt:}")
    private String sttPrompt;

    private WebClient webClient;

    @PostConstruct
    void init() {
        // baseUrl이 /v1를 포함하므로 루트로 자름
        String root = baseUrl.endsWith("/v1") ? baseUrl.substring(0, baseUrl.length()-3) : baseUrl;
        this.webClient = WebClient.builder()
                .baseUrl(root) // https://api.openai.com
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public String transcribe(Path audioPath) {
        try {
            MultiValueMap<String, HttpEntity<?>> body = buildMultipart(audioPath);
            return webClient.post()
                    .uri("/v1/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException("OpenAI STT error " + resp.statusCode() + " :: " + msg))
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
        builder.part("model", sttModel);
        if (sttPrompt != null && !sttPrompt.isBlank()) builder.part("prompt", sttPrompt);

        FileSystemResource fs = new FileSystemResource(audioPath.toFile());
        ContentDisposition cd = ContentDisposition.formData()
                .name("file")
                .filename(fs.getFilename() != null ? fs.getFilename() : "audio.webm")
                .build();

        HttpHeaders fh = new HttpHeaders();
        fh.setContentDisposition(cd);
        fh.setContentType(MediaType.parseMediaType("audio/webm")); // 업로드 포맷에 맞게

        HttpEntity<FileSystemResource> fileEntity = new HttpEntity<>(fs, fh);
        builder.part("file", fileEntity);

        return builder.build();
    }
}
