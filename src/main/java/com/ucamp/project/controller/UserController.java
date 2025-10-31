package com.ucamp.project.controller;

import com.ucamp.project.dto.*;
import com.ucamp.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ucamp.project.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/all")
    public List<UserResponse> getAll() {
        log.info("ㅁㄴㅇㄹㅇㅁㄴㄹ");
        return userService.findAll();
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signUp(@RequestBody SignupRequest request){
        SignupResponse user = userService.signUp(request);
        return ResponseEntity.ok(user);
    }

    // 회원탈퇴 요청. 요청한 userId에 해당하는 유저의 status를 disabled로 변경.
    @PutMapping("/userDel")
    public ResponseEntity<DeleteResponse> deleteUser(@RequestBody DelReq req){
        log.info("userId? :{}", req.getUserId());
        DeleteResponse user = userService.deleteUser(req.getUserId());

        return ResponseEntity.ok(user);
    }

    // 회원 직무 수정 요청. userId와 일치하는 User의 jobId 수정.
    @PatchMapping("/pathJob")
    public ResponseEntity<UpdateResponse> updateUserJob(@RequestBody UpdateRequest request){
        log.info("요청 정보: {}",request.toString());
        UpdateResponse user = userService.updateUserJob(request);
        return ResponseEntity.ok(user);
    }
    // 마이페이지 요청
    // TODO: 소셜 로그인 및 JWT 사용 예정이므로, 임시로 userId = 1로 테스트.
    @GetMapping("/mypage")
    public UserDTO myPage(/*@AuthenticationPrincipal CustomUserDetails userDetails*/) {
        // Long userId = userDetails.getUserId();
        return userService.findUserByUserId(1L);
    }

}
