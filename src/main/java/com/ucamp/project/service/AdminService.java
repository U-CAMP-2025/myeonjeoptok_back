package com.ucamp.project.service;

import com.ucamp.project.dto.UserWithSimulDTO;
import com.ucamp.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    
    // 시뮬레이션 처리 상태 목록으로 조회
    public List<UserWithSimulDTO> findAllTranscriptionStatus() {
        return userRepository.findAllTranscriptionStatus();
    }
}
