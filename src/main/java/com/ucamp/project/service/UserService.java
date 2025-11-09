package com.ucamp.project.service;

import com.ucamp.project.dto.*;
import com.ucamp.project.model.Job;
import com.ucamp.project.dto.UserResponse;
import com.ucamp.project.dto.UserWithCertDTO;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.JobRepository;
import com.ucamp.project.repository.PostRepository;
import com.ucamp.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
        public class UserService {
            private final UserRepository userRepository;
            private final JobRepository jobRepository;
            private final PostRepository postRepository;

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
        Map<String, Object> row = userRepository.findUserWithLatestCertStatus(userId);
        if (row == null || row.isEmpty()) {
            throw new RuntimeException("해당 유저 없음");
        }

        Job job = null;
        if (row.get("jobId") != null) {
            job = Job.builder()
                    .jobId(((Number) row.get("jobId")).longValue())
                    .jobName((String) row.get("jobName"))
                    .build();
        }

        return UserDTO.builder()
                .userId(String.valueOf(((Number) row.get("userId")).longValue()))
                .nickname((String) row.get("nickname"))
                .email((String) row.get("email"))
                .job(job)
                .passStatus(row.get("passStatus") == null ? null : String.valueOf(row.get("passStatus")))
                .status((String) row.get("status"))
                .userProfileImageUrl((String) row.get("userProfileImageUrl"))
                .certStatus(row.get("certStatus") == null ? null : String.valueOf(row.get("certStatus")))
                .build();
    }


    // user role 조회
    public String findUserRoleByUserId(Long userId) {
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));
        return user.getRole();
    }

    public String findUserStatusByUserId(Long userId) {
        User user = userRepository.findByUserId(userId)
             .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));
        return user.getStatus();
    }

    @Transactional
    public String updateUserStatus(Long userId, String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("유효하지 않은 status 값");
        }
        String normalized = status.trim().toUpperCase();
        Set<String> allowed = Set.of("NEW", "ACTIVE", "DISABLED");
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException("status는 NEW, ACTIVE, DISABLED 중 하나여야 합니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없음"));

        user.setStatus(normalized);
        userRepository.save(user);
        return user.getStatus();
    }

    public UserDetailResponse userDetail(Long userId){
        UserDetailDto userDetail = userRepository.findUserDetailById(userId);
        List<Object[]> postRows = postRepository.findPostDetailById(userId);

        List<PostDetailDto> postDetail = postRows.stream()
                .map(row -> {
                    Long currentPostId = ((Number) row[0]).longValue();

                    // 💡 해당 postId로 job 조회
                    List<Object[]> jobRows = jobRepository.findAllById(currentPostId);

                    List<JobDetailDto> jobDetail = jobRows.stream()
                            .map(row2 -> JobDetailDto.builder()
                                    .jobId(((Number) row2[0]).longValue())
                                    .jobName((String) row2[1])
                                    .build())
                            .toList();

                    return PostDetailDto.builder()
                            .postId(currentPostId)
                            .postTitle((String) row[1])
                            .postDescription((String) row[2])
                            .bookmarkCount(row[3] != null ? ((Number) row[3]).longValue() : 0L)
                            .postCreatedAt((String) row[4])
                            .reviewCount(row[5] != null ? ((Number) row[5]).longValue() : 0L)
                            .jobs(jobDetail)
                            .build();
                })
                .toList();


        UserDetailResponse response = new UserDetailResponse();
        response.setUserId(userDetail.getUserId());
        response.setNickname(userDetail.getNickname());
        response.setEmail(userDetail.getEmail());
        response.setUserImageUrl(userDetail.getUsersProfileImageUrl());
        response.setPassStatus(userDetail.getPassStatus());
        response.setJobName(userDetail.getJobName());
        response.setPaymentStatus(userDetail.getPaymentStatus());
        response.setPosts(postDetail);

        return response;
    }
}
