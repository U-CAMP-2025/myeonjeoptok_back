package com.ucamp.project.service;

import com.ucamp.project.dto.CertDTO;
import com.ucamp.project.model.Certificate;
import com.ucamp.project.model.Notification;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.CertRepository;
import com.ucamp.project.repository.NotificationRepository;
import com.ucamp.project.repository.UserRepository;
import com.ucamp.project.sse.SseComponent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertService {
    private final CertRepository certRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final SseComponent sseComponent;
    private final NotificationRepository notificationRepository;

    // 유저 합격 처리
    @Transactional
    public CertDTO trmtCertReq(Long userId, String passStatus) {
        // 1. User 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저 없음"));

        log.info("유저정보?: {}",user.toString());


        // 1. Certificate 조회 (최신 요청 기준)
        Certificate cert = certRepository.findTopByUserUserIdOrderByCertReqDateDesc(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저의 신청 정보 없음"));
        log.info("certificate?: {}", cert.toString());

        // 2. User.passStatus 업데이트
        if ("APPROVED".equalsIgnoreCase(passStatus)) {
            user.setPassStatus("Y");
        } else if ("REJECTED".equalsIgnoreCase(passStatus)) {
            user.setPassStatus("N");
        } else {
            throw new IllegalArgumentException("passStatus는 APPROVED 또는 REJECTED만 가능");
        }

        // 3. Certificate 업데이트
        cert.setCertStatus(passStatus);
        cert.setCertTrmtDate(LocalDateTime.now());

        String message = "합격자 인증이 수락되었습니다.";

        Notification noti = Notification.builder()
                .notiId(null)
                .notiContent(message)
                .user(user)
                .notiType("CERT")
                .notiRead("N")
                .build();

        notificationRepository.save(noti);

        sseComponent.eventtrigger(userId);

        // 4. Response 생성
        return CertDTO.builder()
                .certFileUrl(cert.getCertFileUrl())
                .certTrmtDate(cert.getCertTrmtDate())
                .certStatus(cert.getCertStatus())
                .build();
    }

    // 합격자 인증 생성
    public void createCertificate(Long userId, String fileName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음: " + userId));

        Certificate cert = Certificate.builder()
                .user(user)
                .certFileUrl("temp") // 이미지 접근 경로
                .certStatus("PENDING")
                .certReqDate(LocalDateTime.now())
                .build();

        certRepository.save(cert);

        // ② 임시 파일명을 cert_id 기반 최종 파일명으로 변경
        String finalUrl = fileService.renameTempToCertId("cert", fileName, cert.getCertId());

        // ③ URL 업데이트 (프론트 <img src=...> 그대로 사용 가능)
        cert.setCertFileUrl(finalUrl);
        certRepository.save(cert);
        log.info("Certificate 저장 완료: {}", cert.getCertId());
    }
}
