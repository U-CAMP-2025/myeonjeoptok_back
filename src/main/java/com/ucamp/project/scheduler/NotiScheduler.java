package com.ucamp.project.scheduler;

import com.ucamp.project.model.Notification;
import com.ucamp.project.model.Payments;
import com.ucamp.project.model.Post;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.NotificationRepository;
import com.ucamp.project.repository.PaymentsRepository;
import com.ucamp.project.repository.PostRepository;
import com.ucamp.project.repository.UserRepository;
import com.ucamp.project.service.PostService;
import com.ucamp.project.sse.SseComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotiScheduler {

    private final PaymentsRepository paymentsRepository;
    private final NotificationRepository notificationRepository;
    private final PostRepository postRepository;
    private final SseComponent sseComponent;
    private final PostService postService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 * * * * *")
    public void pushDday() {
        LocalDate today = LocalDate.now();

        // 오늘 기준 D-1 ~ D-7 범위
        LocalDateTime from = today.plusDays(1).atStartOfDay();
        LocalDateTime to   = today.plusDays(7).atTime(23, 59, 59);

        List<Payments> targets =
                paymentsRepository.findActivePaymentsWithMaxExpirationNative(from, to);

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

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void deletePostScheduled() {
        // 결제가 없고 게시물이 10개 이상 있는 사용자들의 ID 목록 조회
        List<Long> userIds = postRepository.findUsersWithPostsAndNoRecentPayments(LocalDateTime.now());

        if (userIds.isEmpty()) {
            return;  // 처리할 사용자가 없다면 종료
        }

        Sort sorted = Sort.by(Sort.Direction.DESC, "postCreatedAt");

        log.info("사용자 만료일 종료 / 포스트 삭제 : " + userIds);

        for (Long userId : userIds) {
            User user = userRepository.findById(userId).get();
            // 사용자의 모든 게시물 조회 (최신 게시물 순으로 정렬)
            List<Post> posts = postRepository.findByUserUserId(userId, Sort.by(Sort.Direction.DESC, "postCreatedAt"));

            // 게시물이 10개 이상일 경우, 9개를 제외한 나머지 삭제
            if (posts.size() > 9) {
                List<Long> postIdsToDelete = posts.stream()
                        .skip(9)  // 처음 9개를 제외하고
                        .map(Post::getPostId)  // 각 게시물의 ID 추출
                        .collect(Collectors.toList());

                // DB에서 해당 게시물들 삭제
                for (Long postId : postIdsToDelete) {
                    postService.deletePost(user,postId);
                }
            }

            String type = "PAY_EXPIRY";
            String content = "이용권이 완료되어 면접노트가 삭제되었습니다. (오래된 순)";

            // 알림 저장
            Notification noti = Notification.builder()
                    .user(user)
                    .notiType(type)
                    .notiContent(content)
                    .notiRead("N")
                    .notiCreatedAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(noti);

            // SSE 신호 전송
            sseComponent.eventtrigger(userId);
        }
    }
}
