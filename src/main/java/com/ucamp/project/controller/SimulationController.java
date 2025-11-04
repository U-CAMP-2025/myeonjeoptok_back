package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.dto.SimulationDetailResponse;
import com.ucamp.project.dto.SimulationResultDto;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.model.Transcription;
import com.ucamp.project.model.User;
import com.ucamp.project.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final TempFileService tempFileService;
    private final SttService sttService;
    private final SimulationQueryService simulationQueryService;
    private final SimulationRecordService simulationRecordService;
    private final TranscriptionService transcriptionService;
    @GetMapping
    public ApiResponse<Object> getPost(@AuthenticationPrincipal User user) {
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
    public ApiResponse<Object> createPost(@RequestBody Simulation simulation, @AuthenticationPrincipal User user) {
        // 비로그인 사용자 요청 예외
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

    @PostMapping("/{simulationId}/answers/{qaId}/audio")
    public ApiResponse<Object> uploadAudio(
            @PathVariable Long simulationId,
            @PathVariable Long qaId,
            @RequestPart("file") MultipartFile file
    ) {
        // 1) 임시 저장
        Path saved = tempFileService.saveToTemp(file, "sim" + simulationId + "_q" + qaId);

        // 2) STT 호출
        String transcript = sttService.transcribe(saved);

        Transcription savedTr = transcriptionService.upsert(simulationId, qaId, transcript);

        // 3) 응답 (url은 필요시 파일 서버나 S3 업로드 후 세팅)
        Map<String, Object> data = new HashMap<>();
        data.put("simulationId", simulationId);
        data.put("qaId", qaId);
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

    @GetMapping("/{simulationId}/result")
    public ApiResponse<Object> getResult(@PathVariable Long simulationId,
                                         @AuthenticationPrincipal User user) {
        if (user == null) {
            return ApiResponse.builder().code(401).message("로그인이 필요합니다.").build();
        }
        // 본인 소유 검증
        var dto = simulationQueryService.buildResult(simulationId); // PostDto + QaDto(transContent 포함)
        return ApiResponse.builder()
                .code(200)
                .message("success")
                .data(dto)
                .build();
    }

    @GetMapping("/records")
    public ApiResponse<Object> getMySimulationRecords(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ApiResponse.builder().code(401).message("로그인이 필요합니다.").build();
        }
        var items = simulationRecordService.listMyRecords(user.getUserId());
        return ApiResponse.builder()
                .code(200)
                .message("success")
                .data(items)
                .build();
    }

    @PutMapping("/{simulationId}/finalize")
    public ApiResponse<Object> finalizeSimulation(@PathVariable Long simulationId,
                                                  @AuthenticationPrincipal User user) {
        if (user == null) {
            return ApiResponse.builder().code(401).message("로그인이 필요합니다.").build();
        }

        simulationService.ensureOwner(simulationId, user.getUserId());

        // 여기서 buildResult()를 통해 최신 데이터 가져오기
        SimulationResultDto result = simulationQueryService.buildResult(simulationId);

        // 시뮬레이션 결과를 Post에 반영
        simulationService.finalizeToPost(result);

        return ApiResponse.builder()
                .code(200)
                .message("success")
                .data("시뮬레이션 결과가 게시글에 저장되었습니다.")
                .build();
    }

    @PatchMapping("/{simulationId}/{qaCount}")
    public ApiResponse<?> stopSimulation(@PathVariable Long simulationId, @PathVariable Long qaCount){

        simulationService.end(simulationId,qaCount);

        return ApiResponse.builder()
                .code(200)
                .message("success")
                .data("ok")
                .build();

    }

}
