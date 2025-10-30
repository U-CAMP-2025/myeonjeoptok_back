package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.model.User;
import com.ucamp.project.service.InterViewerService;
import com.ucamp.project.service.PostService;
import com.ucamp.project.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/simulation")
public class SimulationController {

    private final PostService postService;
    private final SimulationService simulationService;

    @GetMapping
    public ApiResponse<Object> getPost(){
        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(postService.simulGetPost(101l))
                .build();
        return resp;
    }

    @PostMapping
    public ApiResponse<Object> createPost(@RequestBody Simulation simulation){
        System.out.println("TEST : " + simulation.getPost().getPostId());
        System.out.println("TEST : " + simulation.getSimulationRandom());
        System.out.println("TEST : " + simulation.getInterviewer().getInterviewerId());
        simulation.setUser(new User());
        simulation.getUser().setUserId(101l);
        simulation.setSimulationQACount(0l);
        simulation.setSimulationCreatedAt(LocalDateTime.now());
        simulation.setSimulationStatus("INPROGRESS");


        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(simulationService.save(simulation))
                .build();
        return resp;
    }
}
