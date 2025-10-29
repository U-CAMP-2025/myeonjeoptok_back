package com.ucamp.project.service;

import com.ucamp.project.dto.SignupRequest;
import com.ucamp.project.dto.SignupResponse;
import com.ucamp.project.dto.UserResponse;
import com.ucamp.project.model.Job;
import com.ucamp.project.model.User;
import com.ucamp.project.repository.JobRepository;
import com.ucamp.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
        }
        return userResponses;
    }
    public SignupResponse signUp(SignupRequest request){

        Job job = jobRepository.findById(request.getJobId()).orElseThrow(()-> new NoSuchElementException());

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

}
