package com.ucamp.project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BizVerificationService {
    @Value("${odcloud.api.key}")
    private String serviceKey; // application.yml에 등록해둔 키

    public Map<String, Object> verifyBusinessNumber(String bizNum) {
        try {
            String url = "https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=" + serviceKey;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of("b_no", List.of(bizNum));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("사업자등록번호 검증 결과: {}", response.getBody());

            // API 응답 구조에 맞게 결과 추출
            // List<Map<String, Object>> data = (List<Map<String, Object>>) response.getBody().get("data");
            // Map<String, Object> result = data.get(0);

            return response.getBody(); // 예: "계속사업자", "폐업자", 등
        } catch (Exception e) {
            log.error("사업자등록번호 검증 실패", e);
            Map<String, Object> error = new HashMap<>();
            error.put("status_code", "ERROR");
            error.put("message", e.getMessage());
            return error;
        }
    }
}
