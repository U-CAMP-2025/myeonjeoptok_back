package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.dto.SimulationDetailResponse;
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
        System.out.println("TEST : " + simulation.getPostId());
        System.out.println("TEST : " + simulation.getSimulationRandom());
        System.out.println("TEST : " + simulation.getInterviewerId());
        simulation.setUser(new User());
        simulation.getUser().setUserId(101l);


        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(simulationService.save(simulation))
                .build();
        return resp;
    }

    @GetMapping("/{id}")
    public ApiResponse<Object> getSimulation(@PathVariable Long id) {
        SimulationDetailResponse data = simulationService.findDetail(id);
        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
        return resp;
    }
}
