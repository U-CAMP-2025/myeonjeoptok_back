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
            long size = java.nio.file.Files.size(audioPath);

            // (1) 무음 휴리스틱(필요시 임계값 조정)
            boolean likelySilent = size < 10_000; // 기존 2KB -> 10KB로 상향 권장 (WebM 헤더 때문에 약간 크게)

            if (likelySilent) {
                // 완전 무음이면 아예 호출하지 않고 빈 문자열 반환
                return "";
            }

            // (2) 무음이 아니라고 판단될 때만 prompt 포함
            MultiValueMap<String, HttpEntity<?>> body = buildMultipart(audioPath, /* includePrompt */ true);

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
                    .map(this::sanitizeTranscript)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("STT 호출 실패: " + e.getMessage(), e);
        }
    }

    // 결과 후처리(프롬프트 에코 방지)
    private String sanitizeTranscript(String text) {
        String t = (text == null) ? "" : text.trim();
        if (t.isEmpty()) return "";

        if (sttPrompt != null && !sttPrompt.isBlank()) {
            String p = sttPrompt.trim();

            // 완전 일치, 공백 무시 일치, prefix 일치까지 컷
            String tNoSpace = t.replaceAll("\\s+", "");
            String pNoSpace = p.replaceAll("\\s+", "");

            if (t.equals(p) || tNoSpace.equals(pNoSpace) || t.startsWith(p) || tNoSpace.startsWith(pNoSpace)) {
                return "";
            }
        }
        return t;
    }

    // includePrompt를 실제로 사용하도록 수정
    private MultiValueMap<String, HttpEntity<?>> buildMultipart(Path audioPath, boolean includePrompt) {
        var builder = new org.springframework.http.client.MultipartBodyBuilder();
        builder.part("model", sttModel);

        if (includePrompt && sttPrompt != null && !sttPrompt.isBlank()) {
            builder.part("prompt", sttPrompt);
        }

        FileSystemResource fs = new FileSystemResource(audioPath.toFile());
        ContentDisposition cd = ContentDisposition.formData()
                .name("file")
                .filename(fs.getFilename() != null ? fs.getFilename() : "audio.webm")
                .build();

        HttpHeaders fh = new HttpHeaders();
        fh.setContentDisposition(cd);
        fh.setContentType(MediaType.parseMediaType("audio/webm"));

        HttpEntity<FileSystemResource> fileEntity = new HttpEntity<>(fs, fh);
        builder.part("file", fileEntity);

        // (선택) 디코딩이 튀지 않도록 temperature=0 권장
        builder.part("temperature", "0");

        return builder.build();
    }
}
