package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.dto.SimulationDetailResponse;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.model.User;
import com.ucamp.project.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/simulation")
public class SimulationController {

    private final PostService postService;
    private final SimulationService simulationService;
    private final TempFileService  tempFileService;
    private final SttService sttService;

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

        // 1) 임시 저장
        Path saved = tempFileService.saveToTemp(file, "sim" + simulationId + "_q" + questionIndex);

        // 2) STT 호출
        String transcript = sttService.transcribe(saved);

        // 3) 응답 (url은 필요시 파일 서버나 S3 업로드 후 세팅)
        Map<String, Object> data = new HashMap<>();
        data.put("simulationId", simulationId);
        data.put("qIdx", questionIndex);
        data.put("originalName", file.getOriginalFilename());
        data.put("size", file.getSize());
        data.put("contentType", file.getContentType());
        data.put("transcript", transcript); // ★ 프론트로 전사 텍스트 전달

        return ApiResponse.builder()
                .code(200)
                .message("success")
                .data(data)
                .build();
    }


}
