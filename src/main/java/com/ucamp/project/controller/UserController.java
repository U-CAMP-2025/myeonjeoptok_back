package com.ucamp.project.controller;

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

    @PostMapping("/regist")
    public String regist() {
        return "";
    }
}
