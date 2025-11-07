package com.ucamp.project.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrOpenService {

    private final BizVerificationService bizVerificationService;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + System.getenv("OPENAI_API_KEY"))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build();

    private static final String OCR_MODEL = "gpt-4o-mini";

    public Map<String, Object> detectText(String imageUrl) {
        Map<String, Object> responseMap = new HashMap<>();

        try {
            boolean useBase64 = false;

            // 1️⃣ 로컬 URL 즉시 감지
            if (imageUrl.contains("localhost") || imageUrl.contains("127.0.0.1") || imageUrl.contains(":8080")) {
                useBase64 = true;
                log.info("⚠️ 로컬 URL 감지 → Base64 사용 예정: {}", imageUrl);
            } else {
                // 2️⃣ 외부 접근 가능성 테스트
                try {
                    HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
                    conn.setRequestMethod("HEAD");
                    conn.setConnectTimeout(2000);
                    conn.setReadTimeout(2000);
                    int code = conn.getResponseCode();
                    if (code != 200) {
                        useBase64 = true;
                        log.warn("⚠️ 외부 접근 불가 (HTTP {}) → Base64 fallback 적용", code);
                    } else {
                        log.info("✅ 외부 접근 가능 → URL 그대로 사용");
                    }
                } catch (Exception e) {
                    log.warn("⚠️ 외부 접근 실패({}) → Base64 fallback 적용", e.getMessage());
                    useBase64 = true;
                }
            }

            // (2) image_url 생성
            String imagePayload;
            if (useBase64) {
                URL url = new URL(imageUrl);
                byte[] data;
                try (InputStream in = url.openStream()) {
                    data = in.readAllBytes();
                }
                String base64 = Base64.getEncoder().encodeToString(data);
                imagePayload = String.format("{\"url\": \"data:image/png;base64,%s\"}", base64);
            } else {
                imagePayload = String.format("{\"url\": \"%s\"}", imageUrl);
            }

            // (3) OpenAI OCR 요청 JSON
            String jsonBody = """
            {
              "model": "%s",
              "messages": [
                {
                  "role": "system",
                  "content": "너는 한국어 문서를 읽을 수 있는 OCR 모델이다. 이미지의 모든 글자를 인식해라. 사업자등록번호가 있다면 반드시 포함시켜라."
                },
                {
                  "role": "user",
                  "content": [
                    {"type": "text", "text": "이 이미지를 OCR로 분석해라."},
                    {"type": "image_url", "image_url": %s}
                  ]
                }
              ]
            }
            """.formatted(OCR_MODEL, imagePayload);

            // (4) OpenAI 요청 전송
            String result = webClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(jsonBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class).map(msg ->
                                    new RuntimeException("OpenAI OCR error " + resp.statusCode() + " :: " + msg))
                    )
                    .bodyToMono(String.class)
                    .block();

            // (5) 응답 파싱
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(result);
            String text = root.path("choices").get(0).path("message").path("content").asText().trim();
            responseMap.put("text", text);
            log.info("OCR 인식 텍스트:\n{}", text);

            // (6) 사업자등록번호 추출
            Pattern pattern = Pattern.compile("(\\d{3}\\D*\\d{2}\\D*\\d{5})");
            Matcher matcher = pattern.matcher(text.replaceAll("\\s+", " "));

            if (matcher.find()) {
                String bizNum = matcher.group(1).replaceAll("\\D", "");
                log.info("추출된 사업자번호: {}", bizNum);
                responseMap.put("bizNum", bizNum);

                // (7) 검증 API 호출
                Map<String, Object> verfRes = bizVerificationService.verifyBusinessNumber(bizNum);
                responseMap.put("verfRes", verfRes);

                // (8) 상태 판정
                List<?> dataList = (List<?>) verfRes.get("data");
                String bizStatus = null;
                String taxType = null;

                if (dataList != null && !dataList.isEmpty()) {
                    Map<?, ?> first = (Map<?, ?>) dataList.get(0);
                    bizStatus = String.valueOf(first.get("b_stt"));
                    taxType = String.valueOf(first.get("tax_type"));
                }

                boolean isValid = "계속사업자".equals(bizStatus)
                        && taxType != null
                        && !taxType.contains("등록되지 않은");

                responseMap.put("status", isValid ? "success" : "fail");

            } else {
                log.info("사업자번호 인식 실패");
                responseMap.put("bizNum", "NOT_FOUND");
                responseMap.put("status", "fail");
                responseMap.put("verfRes", Map.of("data", Collections.emptyList()));
            }

        } catch (Exception e) {
            log.error("OCR 처리 중 오류", e);
            responseMap.put("status", "fail");
            responseMap.put("error", e.getMessage());
        }

        return responseMap;
    }
}



