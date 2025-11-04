package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.model.Interviewer;
import com.ucamp.project.service.InterViewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interviewers")
public class InteriewerController {

    private final InterViewerService interViewerService;

    @GetMapping
    public ApiResponse<Object> interviewers(){
        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(interViewerService.findAll())
                .build();
        return resp;
    }
}
