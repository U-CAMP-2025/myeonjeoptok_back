package com.ucamp.project.service;

import com.ucamp.project.dto.UserResponse;
import com.ucamp.project.dto.UserWithCertDTO;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserResponse> findAll() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponses = new ArrayList<>();
        for (User user : users) {
            UserResponse userResponse = UserResponse.builder()
                    .nickname(user.getNickname())
                    .email(user.getEmail()).build();
            userResponses.add(userResponse);
        }
        return userResponses;
    }

    public List<UserResponse> findAllWithCertAndSimulInfo() {
        return userRepository.findAllWithCertAndSimulInfo();
    }

    public List<UserWithCertDTO> findAllWithCert() { ;
        return userRepository.findAllWithCert();
    }
}
