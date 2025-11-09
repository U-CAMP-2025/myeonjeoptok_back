package com.ucamp.project.scheduler;

import com.ucamp.project.model.Notification;
import com.ucamp.project.model.Payments;
import com.ucamp.project.repository.NotificationRepository;
import com.ucamp.project.repository.PaymentsRepository;
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

    private final PaymentsRepository paymentsRepository;
    private final NotificationRepository notificationRepository;
    private final SseComponent sseComponent;

    @Scheduled(cron = "0 0 0 * * *")
    public void pushDday() {
        LocalDate today = LocalDate.now();

        // 오늘 기준 D-1 ~ D-7 범위
        LocalDateTime from = today.plusDays(1).atStartOfDay();
        LocalDateTime to   = today.plusDays(7).atTime(23, 59, 59);

        List<Payments> targets =
                paymentsRepository.findByPaymentStatusAndExpiredAtBetween("ACTIVE", from, to);

        int saved = 0, skipped = 0;

        for (Payments p : targets) {
            LocalDate expiryDate = p.getExpiredAt().toLocalDate();
            long daysLeft = ChronoUnit.DAYS.between(today, expiryDate);

            if (daysLeft < 1 || daysLeft > 7) {
                skipped++;
                continue;
            }

            String type = "PAY_EXPIRY";
            String content = String.format("구독 만료 %d일 전입니다. (만료일: %s)", daysLeft, expiryDate);

            // 오늘 같은 내용의 알림이 이미 있으면 스킵
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
                    .notiCreatedAt(LocalDateTime.now()) // 시스템 로컬시간 기준
                    .build();
            notificationRepository.save(noti);
            saved++;

            // SSE 신호 전송
            sseComponent.eventtrigger(p.getUser().getUserId());
        }

        log.info("[NotiScheduler] candidates={}, saved={}, skipped={}", targets.size(), saved, skipped);
    }
}
