package com.ucamp.project.controller;

import com.ucamp.project.dto.CertDTO;
import com.ucamp.project.dto.UserResponse;
import com.ucamp.project.dto.UserWithCertDTO;
import com.ucamp.project.dto.UserWithSimulDTO;
import com.ucamp.project.service.AdminService;
import com.ucamp.project.service.CertService;
import com.ucamp.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final CertService certService;
    private final AdminService adminService;

    // 유저 전체 조회
    @GetMapping("/users")
    public Page<UserResponse> findAllUsers(Pageable pageable) {
        log.info("요청 들어옴?");
        return userService.findAllWithCertAndSimulInfo(pageable);
    }
    // '합격자' 인증 신청한 유저 조회
    @GetMapping("/users/certificate")
    public List<UserWithCertDTO> findCertReqUsers() {
        return userService.findAllWithCert();
    }

    // '합격자' 요청 처리
    @PutMapping("/pathPass")
    public CertDTO trmtCertReq(@RequestBody Map<String, String> request) {
        Long userId = Long.valueOf(request.get("userId"));
        String passStatus = request.get("passStatus");

        return certService.trmtCertReq(userId, passStatus);
    }

    // 시뮬레이션 처리 상태를 목록으로 조회
    @GetMapping("/transcription")
    public Page<UserWithSimulDTO> findAllTranscriptionStatus(Pageable pageable) {
        return adminService.findAllTranscriptionStatus(pageable);
    }



}
