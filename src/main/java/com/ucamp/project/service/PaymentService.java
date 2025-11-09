package com.ucamp.project.service;

import com.ucamp.project.model.Payments;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    // 유저의 최근 결제 내역 하나
    public Payments findLatestByUserId(Long userId) {
        return paymentRepository.findTopByUser_UserIdOrderByApprovedAtDesc(userId)
                .orElse(null);
    }

    // 유저의 모든 결제 내역
    public List<Payments> findAllByUserId(Long userId) {
        return paymentRepository.findByUser_UserIdOrderByApprovedAtDesc(userId);
    }

    // 결제 완료 데이터 저장
    public Payments savePaymentFromToss(JSONObject tossResponse, Long userId) {
        // 필드 추출
        String orderId = (String) tossResponse.get("orderId");
        String paymentKey = (String) tossResponse.get("paymentKey");
        Long totalAmount = Long.valueOf(tossResponse.getAsNumber("totalAmount").longValue());

        // 승인일자 문자열 파싱
        String approvedAtStr = (String) tossResponse.get("approvedAt");
        LocalDateTime approvedAt = LocalDateTime.parse(approvedAtStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Payments payment = Payments.builder()
                .user(User.builder().userId(userId).build())
                .orderId(orderId) // orderId, front에서 UUID로 설정돼서 넘어옴
                .paymentKey(paymentKey) // paymentKey, 필수값, 자동 설정
                .totalAmount(totalAmount) // 결제금액
                .approvedAt(approvedAt) // 결제일시
                .expiredAt(approvedAt.plusMonths(1)) // 만료일시. 기본 1개월
                .paymentStatus("ACTIVE") // 결제 상태. 기본 ACTIVE
                .build();

        return paymentRepository.save(payment);
    }
}
