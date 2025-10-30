package com.ucamp.project.controller;

import com.ucamp.project.dto.JobResponse;
import com.ucamp.project.service.JobService;
import com.ucamp.project.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
//

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;
    // 직무 목록 조회 (회원가입 폼에서 필요)
    @GetMapping("/jobs")
    public ResponseEntity<List<JobResponse>> findAllJobs(){
        List<JobResponse> jobList = jobService.findAll();
        return ResponseEntity.ok(jobList);

    }
}
