// AiFeedbackService.java
package com.ucamp.project.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiFeedbackService {

    @Value("${openai.apiKey}")
    private String apiKey;

    @Value("${openai.baseUrl}")
    private String baseUrl; // ex) https://api.openai.com/v1

    @Value("${openai.feedback.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.feedback.maxLen:1000}")
    private int maxLen;

    @Value("${openai.feedback.temperature:0.3}")
    private double temperature;

    @Value("${openai.feedback.prompt}")
    private String feedbackPrompt; // 위 yml에 넣은 멀티라인 프롬프트

    private WebClient webClient;

    @PostConstruct
    void init() {
        String root = baseUrl.endsWith("/v1") ? baseUrl.substring(0, baseUrl.length()-3) : baseUrl;
        this.webClient = WebClient.builder()
                .baseUrl(root) // https://api.openai.com
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    public String generateFeedback(String question, String transcript) {
        if (question == null) question = "";
        if (transcript == null) transcript = "";

        String system = "너는 한국어 면접 코치다. 반드시 하나의 문자열만 출력한다.";
        String user = String.format("%s%n%n[질문]%n%s%n%n[지원자 STT 답변]%n%s",
                feedbackPrompt, question, transcript);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", user)
                ),
                "temperature", temperature
        );

        try {
            Map resp = webClient.post()
                    .uri("/v1/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, r ->
                            r.bodyToMono(String.class).flatMap(msg ->
                                    Mono.error(new RuntimeException("OpenAI feedback error " + r.statusCode() + " :: " + msg)))
                    )
                    .bodyToMono(Map.class)
                    .block();

            if (resp == null) return "";
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
            if (choices == null || choices.isEmpty()) return "";

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            if (content == null) return "";

            if (content.length() > maxLen) content = content.substring(0, maxLen);
            return content.trim();
        } catch (Exception e) {
            log.warn("AI feedback generate failed: {}", e.getMessage());
            return "";
        }
    }
}
