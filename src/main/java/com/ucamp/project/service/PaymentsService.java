package com.ucamp.project.service;

import com.ucamp.project.model.Payments;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.PaymentsRepository;
import jakarta.transaction.Transactional;
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
public class PaymentsService {
    private final PaymentsRepository paymentsRepository;

    // 유저의 최근 결제 내역 하나
    public Payments findLatestByUserId(Long userId) {
        return paymentsRepository.findTopByUser_UserIdOrderByApprovedAtDesc(userId)
                .orElse(null);
    }

    // 특정 유저의 모든 결제 내역
    public List<Payments> findAllByUserId(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        // 해당 유저의 결제 내역 전부 조회 (최신순)
        List<Payments> payments = paymentsRepository.findByUser_UserIdOrderByExpiredAtDesc(userId);

        // 만료일 지난 결제는 DISABLED 처리
        payments.stream()
                .filter(p -> p.getExpiredAt().isBefore(now))
                .filter(p -> !"DISABLED".equals(p.getPaymentStatus()))
                .forEach(p -> p.setPaymentStatus("DISABLED"));

        // 변경사항 저장
        paymentsRepository.saveAll(payments);

        return payments;
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

        return paymentsRepository.save(payment);
    }

    // 결제 완료 데이터 저장
    @Transactional
    public Payments createPayment(JSONObject tossResponse, Long userId) {

        // ===== 1️⃣ 기본 필드 추출 =====
        String orderId = (String) tossResponse.get("orderId");
        String paymentKey = (String) tossResponse.get("paymentKey");
        Long totalAmount = Long.valueOf(tossResponse.getAsNumber("totalAmount").longValue());

        // 승인일자 파싱
        String approvedAtStr = (String) tossResponse.get("approvedAt");
        LocalDateTime tossApprovedAt = LocalDateTime.parse(approvedAtStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        LocalDateTime now = LocalDateTime.now();

        // ===== 2️⃣ 유저 최신 결제 조회 =====
        Payments latest = paymentsRepository.findTopByUser_UserIdOrderByExpiredAtDesc(userId).orElse(null);

        LocalDateTime startedAt;
        LocalDateTime expiredAt;
        LocalDateTime approvedAt;
        // String status;

        // ===== 3️⃣ 신규 / 갱신 분기 =====
        if (latest != null && !latest.getExpiredAt().isBefore(now.minusDays(31))) {
            // ✅ 갱신
            startedAt = latest.getExpiredAt().plusDays(1);  // 새 구독 시작일은 이전 만료일+1
            expiredAt = startedAt.plusMonths(1);            // 새 만료일은 거기에 +1개월
            approvedAt = now;                               // 결제일은 현재 시간
            // status = "RENEWED";
            System.out.println("🔁 구독 갱신 처리됨");
        } else {
            // ✅ 신규
            startedAt = now;
            expiredAt = now.plusMonths(1);
            approvedAt = now; // tossApprovedAt도 동일시 가능
            // status = "NEW";
            System.out.println("🆕 신규 결제 처리됨");
        }

        // ===== 4️⃣ 엔티티 생성 =====
        Payments payment = Payments.builder()
                .user(User.builder().userId(userId).build())
                .orderId(orderId)
                .paymentKey(paymentKey)
                .totalAmount(totalAmount)
                .approvedAt(approvedAt)
                .startedAt(startedAt)
                .expiredAt(expiredAt)
                .paymentStatus("ACTIVE")
                .build();

        // ===== 5️⃣ 저장 =====
        return paymentsRepository.save(payment);
    }
}
