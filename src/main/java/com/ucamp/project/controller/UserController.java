package com.ucamp.project.controller;

import com.ucamp.project.dto.*;
import com.ucamp.project.model.User;
import com.ucamp.project.service.CertService;
import com.ucamp.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.ucamp.project.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final CertService certService;

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
    @PutMapping("/pathJob")
    public ResponseEntity<UpdateResponse> updateUserJob(@RequestBody UpdateRequest request){
        log.info("요청 정보: {}",request.toString());
        UpdateResponse user = userService.updateUserJob(request);
        return ResponseEntity.ok(user);
    }

    // 마이페이지 요청
    @GetMapping("/mypage")
    public ResponseEntity<UserDTO> myPage() {
        return ResponseEntity.ok(userService.findUserByUserId(1L));
    }

    // '합격자' 신청
    @PostMapping("/apply")
    public ResponseEntity<String> apply(
            @RequestBody Map<String, String> body) {
        String fileName = body.get("fileName");
        log.info("파일이름?(USerController): {}", fileName);

        certService.createCertificate(1L, fileName);
        return ResponseEntity.ok("합격자 신청이 완료되었습니다.");
    }


}
