package com.ucamp.project.controller;

import com.ucamp.project.dto.SignupRequest;
import com.ucamp.project.dto.SignupResponse;
import com.ucamp.project.dto.UserResponse;
import com.ucamp.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserResponse> getAll() {
        return userService.findAll();
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signUp(@RequestBody SignupRequest request){
        SignupResponse user = userService.signUp(request);
        return ResponseEntity.ok(user);
    }
}
