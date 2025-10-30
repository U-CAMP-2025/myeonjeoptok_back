package com.ucamp.project.service;

import com.ucamp.project.dto.CertDTO;
import com.ucamp.project.model.Certificate;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.CertRepository;
import com.ucamp.project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CertService {
    private final CertRepository certRepository;
    private final UserRepository userRepository;

    // 유저 합격 처리
    @Transactional
    public CertDTO trmtCertReq(Long userId, String passStatus) {
        // 1. User 조회 후 해당 유저의 passStatus 업데이트
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저 없음"));


        // 1. Certificate 조회 (최신 요청 기준)
        Certificate cert = certRepository.findByUserUserId(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저의 신청 정보 없음"));

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

        // 4. Response 생성
        return CertDTO.builder()
                .certFileUrl(cert.getCertFileUrl())
                .certTrmtDate(cert.getCertTrmtDate())
                .certStatus(cert.getCertStatus())
                .build();
    }
}
