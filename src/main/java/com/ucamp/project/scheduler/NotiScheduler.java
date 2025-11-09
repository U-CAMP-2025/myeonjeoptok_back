package com.ucamp.project.scheduler;

import com.ucamp.project.model.Notification;
import com.ucamp.project.model.Payments;
import com.ucamp.project.repository.NotificationRepository;
import com.ucamp.project.repository.PaymentRepository;
import com.ucamp.project.sse.SseComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotiScheduler {

    private final PaymentRepository paymentRepository;
    private final NotificationRepository notificationRepository;
    private final SseComponent sseComponent;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void pushDday() {
        ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(kst);

        // 오늘 기준 D-1 ~ D-7 범위
        LocalDateTime from = today.plusDays(1).atStartOfDay();          // 내일 00:00
        LocalDateTime to   = today.plusDays(7).atTime(23, 59, 59);      // 7일 후 23:59:59

        List<Payments> targets =
                paymentRepository.findByPaymentStatusAndExpiredAtBetween("ACTIVE", from, to);

        int saved = 0, skipped = 0;

        for (Payments p : targets) {
            LocalDate expiryDate = p.getExpiredAt().atZone(kst).toLocalDate();
            long daysLeft = ChronoUnit.DAYS.between(today, expiryDate); // 1..7

            if (daysLeft < 1 || daysLeft > 7) {
                skipped++;
                continue;
            }

            String type = "SUBSCRIPTION_EXPIRY";
            String content = String.format("구독 만료 %d일 전입니다. (만료일: %s)", daysLeft, expiryDate);

            // 중복 방지: 같은 날 같은 내용 있으면 스킵
            LocalDateTime startOfDay = today.atStartOfDay();
            LocalDateTime endOfDay   = today.atTime(23, 59, 59);

            boolean already = notificationRepository
                    .existsByUser_UserIdAndNotiTypeAndNotiContentAndNotiCreatedAtBetween(
                            p.getUser().getUserId(), type, content, startOfDay, endOfDay);

            if (already) {
                skipped++;
                continue;
            }

            // 알림 저장
            Notification noti = Notification.builder()
                    .user(p.getUser())
                    .notiType(type)
                    .notiContent(content)
                    .notiRead("N")
                    .notiCreatedAt(LocalDateTime.now(kst))
                    .build();
            notificationRepository.save(noti);
            saved++;

            // 실시간 신호 (프론트는 이 신호를 받으면 /api/notifications 재조회)
            sseComponent.eventtrigger(p.getUser().getUserId());
        }

        log.info("[NotiScheduler] candidates={}, saved={}, skipped={}", targets.size(), saved, skipped);
    }
}
