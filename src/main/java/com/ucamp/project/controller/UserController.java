package com.ucamp.project.controller;

import com.ucamp.project.dto.*;
import com.ucamp.project.model.User;
import com.ucamp.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ucamp.project.dto.UserResponse;
import com.ucamp.project.model.Job;
import com.ucamp.project.service.UserService;
import lombok.RequiredArgsConstructor;
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

    @DeleteMapping("/userDel")
    public ResponseEntity<DeleteResponse> deleteUser(@RequestParam("userId") Long userId){
        DeleteResponse user = userService.deleteUser(userId);

        return ResponseEntity.ok(user);
    }

    @PutMapping("/userUpdate")
    public ResponseEntity<UpdateResponse> updateUser(@RequestBody UpdateRequest request){
        UpdateResponse user = userService.updateUser(request);

        return ResponseEntity.ok(user);
    @PostMapping("/regist")
    public String regist() {
        return "";
    }
}
