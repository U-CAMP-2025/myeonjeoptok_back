package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.dto.SimulationDetailResponse;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.model.User;
import com.ucamp.project.service.InterViewerService;
import com.ucamp.project.service.PostService;
import com.ucamp.project.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/simulation")
public class SimulationController {

    private final PostService postService;
    private final SimulationService simulationService;

    @GetMapping
    public ApiResponse<Object> getPost(@AuthenticationPrincipal User user){
        // 비로그인 사용자 요청 예외
        if (user == null) {
            return ApiResponse.builder()
                    .code(401)
                    .message("로그인이 필요합니다.")
                    .build();
        }

        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(postService.simulGetPost(user.getUserId()))
                .build();
        return resp;
    }

    @PostMapping
    public ApiResponse<Object> createPost(@RequestBody Simulation simulation,  @AuthenticationPrincipal User user){
        // 비로그인 사용자 요청 예외 처리
        if (user == null) {
            return ApiResponse.builder()
                    .code(401)
                    .message("로그인이 필요합니다.")
                    .build();
        }
        
        System.out.println("TEST : " + simulation.getPostId());
        System.out.println("TEST : " + simulation.getSimulationRandom());
        System.out.println("TEST : " + simulation.getInterviewerId());

        simulation.setUser(user);
//        simulation.setUser(new User());
//        simulation.getUser().setUserId(101l);


        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(simulationService.save(simulation))
                .build();
        return resp;
    }

    @GetMapping("/{id}/start")
    public ApiResponse<Object> getSimulation(@PathVariable Long id) {
        SimulationDetailResponse data = simulationService.findDetail(id);
        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
        return resp;
    }

    @PostMapping("/{simulationId}/answers/{qIdx}/audio")
    public ApiResponse<Object> uploadAudio(
            @PathVariable Long simulationId,
            @PathVariable Long qIdx,
            @RequestPart("file") MultipartFile file
    ) {
        long questionIndex = qIdx + 1;
        System.out.println("[UPLOAD REQUEST RECEIVED]");
        System.out.println("simulationId = " + simulationId);
        System.out.println("qIdx = " + questionIndex);
        System.out.println("file name = " + file.getOriginalFilename());
        System.out.println("file size = " + file.getSize());
        System.out.println("file content type = " + file.getContentType());

        // MultipartFile 자체를 반환하면 직렬화 에러 -> 메타데이터만 반환
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("simulationId", simulationId);
        fileInfo.put("qIdx", questionIndex);
        fileInfo.put("originalName", file.getOriginalFilename());
        fileInfo.put("size", file.getSize());
        fileInfo.put("contentType", file.getContentType());

        ApiResponse<Object> resp = ApiResponse.builder()
                .code(200)
                .message("success")
                .data(fileInfo)
                .build();

        return resp;
    }

}
