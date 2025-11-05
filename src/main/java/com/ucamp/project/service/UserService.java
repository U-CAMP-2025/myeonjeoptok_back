package com.ucamp.project.service;

import com.ucamp.project.dto.*;
import com.ucamp.project.model.Job;
import com.ucamp.project.dto.UserResponse;
import com.ucamp.project.dto.UserWithCertDTO;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.JobRepository;
import com.ucamp.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
        public class UserService {
            private final UserRepository userRepository;
            private final JobRepository jobRepository;

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
    public SignupResponse signUp(SignupRequest request){

        Job job = jobRepository.findById(request.getJobId()).orElseThrow(()-> new NoSuchElementException("유효하지 않은 직무 ID"));

        User user = User.builder()
                .nickname(request.getNickname())
                .email(request.getEmail())
                .job(job)
                .kakaoId(request.getKakaoId())
                .build();
        User saveUser = userRepository.save(user);

        return SignupResponse.builder()
                .userId(saveUser.getUserId())
                .nickname(saveUser.getNickname())
                .email(saveUser.getEmail())
                .jobId(saveUser.getJob().getJobId())
                .kakaoId(saveUser.getKakaoId())
                .build();
    }
    public DeleteResponse deleteUser(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없음"));

        user.setStatus("DISABLED");
        userRepository.save(user);

        return DeleteResponse.builder()
                .status("SUCCESS")
                .message("회원탈퇴 완료!")
                .build();
    }
    public UpdateResponse updateUserJob(UpdateRequest request) {
        User findUser = userRepository.findById(request.getUserId()).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없음"));

        Job findJob = jobRepository.findById(request.getJobId()).orElseThrow(() -> new NoSuchElementException("유효하지 않은 직무 ID"));

        findUser.setJob(findJob);

        userRepository.save(findUser);

        return UpdateResponse.builder()
                .jobId(findUser.getJob().getJobId())
                .jobName(findUser.getJob().getJobName())
                .build();
    }
    //
    public Page<UserResponse> findAllWithCertAndSimulInfo(Pageable pageable) {
        return userRepository.findAllWithCertAndSimulInfo(pageable);
    }

    public List<UserWithCertDTO> findAllWithCert() { ;
        return userRepository.findAllWithCert();
    }

    // userId로 User 조회
    public UserDTO findUserByUserId(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("해당 유저 없음"));

        return UserDTO.builder()
                .userId(String.valueOf(user.getUserId()))
                .nickname(user.getNickname())
                .email(user.getEmail())
                .job(user.getJob())
                .passStatus(user.getPassStatus())
                .status(user.getStatus())
                .userProfileImageUrl(user.getUsersProfileImageUrl())
                .build();
    }

    // user role 조회
    public String findUserRoleByUserId(Long userId) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));
        return user.getRole();
    }
}
