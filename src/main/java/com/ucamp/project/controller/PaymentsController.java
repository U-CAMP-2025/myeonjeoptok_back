package com.ucamp.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ucamp.project.dto.PaymentDTO;
import com.ucamp.project.model.Payments;
import com.ucamp.project.model.User;
import com.ucamp.project.service.PaymentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/payment")
@Slf4j
@RequiredArgsConstructor
public class PaymentsController {


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PaymentsService paymentsService;

    /**
     * 로그인한 유저의 결제 내역 전체 조회
     */
    @GetMapping("/history")
    public ResponseEntity<?> getMyPaymentHistory(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요한 요청입니다."));
        }

        List<Payments> payments = paymentsService.findAllByUserId(user.getUserId());
        if (payments.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<PaymentDTO> responses = payments.stream()
                .sorted(Comparator.comparing(Payments::getApprovedAt).reversed())
                .map(p -> PaymentDTO.builder()
                        .orderId(p.getOrderId())
                        .paymentKey(p.getPaymentKey())
                        .approvedAt(p.getApprovedAt())
                        .startedAt(p.getStartedAt())
                        .expiredAt(p.getExpiredAt())
                        .totalAmount(p.getTotalAmount())
                        .paymentStatus(p.getPaymentStatus())
                        .build())
                .toList();

        return ResponseEntity.ok(responses);
    }

    // 로그인한 사용자의 최근 결제 내역 조회 (단일)
    @GetMapping("/history/me")
    public ResponseEntity<?> getMyLatestPayment(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "로그인이 필요한 요청입니다."));
        }

        Payments payment = paymentsService.findLatestByUserId(user.getUserId());
        if (payment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "결제 내역이 없습니다."));
        }

        PaymentDTO response = PaymentDTO.builder()
                .orderId(payment.getOrderId())
                .paymentKey(payment.getPaymentKey())
                .approvedAt(payment.getApprovedAt())
                .startedAt(payment.getStartedAt())
                .expiredAt(payment.getExpiredAt())
                .totalAmount(payment.getTotalAmount())
                .paymentStatus(payment.getPaymentStatus())
                .build();

        return ResponseEntity.ok(response);
    }

    // 특정 유저 ID로 결제 내역 전체 보기 (관리자용)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPaymentsByUserId(@PathVariable Long userId,
                                                 @AuthenticationPrincipal User user) {
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "접근 권한이 없습니다."));
        }

        List<Payments> payments = paymentsService.findAllByUserId(userId);
        if (payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "해당 유저의 결제 내역이 없습니다."));
        }

        List<PaymentDTO> responses = payments.stream()
                .map(p -> PaymentDTO.builder()
                        .orderId(p.getOrderId())
                        .paymentKey(p.getPaymentKey())
                        .approvedAt(p.getApprovedAt())
                        .startedAt(p.getStartedAt())
                        .expiredAt(p.getExpiredAt())
                        .totalAmount(p.getTotalAmount())
                        .paymentStatus(p.getPaymentStatus())
                        .build())
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/confirm")
    public ResponseEntity<JSONObject> confirmPayment(@RequestBody String jsonBody,
                                                     @AuthenticationPrincipal User user) throws Exception {
        // 로그인 여부 확인
        if (user == null) {
            JSONObject error = new JSONObject();
            error.put("message", "로그인이 필요한 요청입니다.");
            error.put("code", 401);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        JSONParser parser = new JSONParser();

        JSONObject requestData = (JSONObject) parser.parse(jsonBody);
        String paymentKey = (String) requestData.get("paymentKey");
        String orderId = (String) requestData.get("orderId");
        String amount = (String) requestData.get("amount");

        log.info("[결제확인 요청] orderId={}, paymentKey={}, amount={}", orderId, paymentKey, amount);

        // ① Toss Payments API 호출
        JSONObject confirmBody = new JSONObject();
        confirmBody.put("orderId", orderId);
        confirmBody.put("amount", amount);
        confirmBody.put("paymentKey", paymentKey);

        String secretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6";
        String auth = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        URL url = new URL("https://api.tosspayments.com/v1/payments/confirm");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Basic " + auth);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(confirmBody.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        boolean isSuccess = (code == 200);

        InputStream responseStream = isSuccess ? conn.getInputStream() : conn.getErrorStream();
        JSONObject tossResponse = (JSONObject) parser.parse(new InputStreamReader(responseStream, StandardCharsets.UTF_8));
        log.info("토스 응답 내용 (Pretty):\n{}", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(tossResponse));

        if (isSuccess) {
            // ② 결제 데이터 DB 저장
            // paymentsService.savePaymentFromToss(tossResponse, user.getUserId());
            paymentsService.createPayment(tossResponse, user.getUserId());
        }

        return ResponseEntity.status(code).body(tossResponse);
    }
}