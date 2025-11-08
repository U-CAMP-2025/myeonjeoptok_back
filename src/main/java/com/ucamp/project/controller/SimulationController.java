package com.ucamp.project.controller;

import com.ucamp.project.dto.ApiResponse;
import com.ucamp.project.dto.FinalizeRequest;
import com.ucamp.project.dto.QaDto;
import com.ucamp.project.dto.SimulationDetailResponse;
import com.ucamp.project.model.Simulation;
import com.ucamp.project.model.Transcription;
import com.ucamp.project.model.User;
import com.ucamp.project.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/simulation")
@Slf4j
public class SimulationController {

    private final PostService postService;
    private final SimulationService simulationService;
    private final TempFileService tempFileService;
    private final SttService sttService;
    private final SimulationQueryService simulationQueryService;
    private final SimulationRecordService simulationRecordService;
    private final TranscriptionService transcriptionService;
    private final AiFeedbackService aiFeedbackService;

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
        SimulationDetailResponse data = simulationService.findStart(id);
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
        log.info("[UPLOAD] simId={}, qaId={}, name={}, size={}, type={}",
                simulationId, qaId, file.getOriginalFilename(), file.getSize(), file.getContentType());
        try {
            Path saved = tempFileService.saveToTemp(file, "sim" + simulationId + "_q" + qaId);
            log.info("[UPLOAD] saved temp path={}", saved);

            String transcript = sttService.transcribe(saved);
            log.info("[STT] simId={}, qaId={}, transcript.len={}", simulationId, qaId,
                    (transcript == null ? 0 : transcript.length()));
            transcript = (transcript == null) ? "" : transcript.trim();

            Transcription tr = transcriptionService.upsert(simulationId, qaId, transcript);
            log.info("[UPSERT] trId={}, completedAt={}", tr.getTrId(), tr.getCompletedAt());

            // 질문 조회
            String question = null;
            try {
                var detail = simulationService.findDetail(simulationId);
                question = detail.getPost().getQaList().stream()
                        .filter(q -> qaId.equals(q.getQaId()))
                        .findFirst()
                        .map(q -> q.getQaQuestion())
                        .orElse(null);
                log.info("[QUESTION] found={}, len={}", (question != null), (question == null ? 0 : question.length()));
            } catch (Exception e) {
                log.warn("[QUESTION] failed to resolve question. simId={}, qaId={}, msg={}",
                        simulationId, qaId, e.getMessage(), e);
            }

            // 피드백
            String feedback = "";
            if (StringUtils.hasText(question) && StringUtils.hasText(transcript)) {
                feedback = aiFeedbackService.generateFeedback(question.trim(), transcript);
                if (StringUtils.hasText(feedback)) {
                    transcriptionService.updateFeedback(tr.getTrId(), feedback);
                } else {
                }
            } else {
                log.warn("[FEEDBACK] skip. question or transcript empty. q.len={}, t.len={}",
                        (question == null ? 0 : question.length()), transcript.length());
            }

            Map<String, Object> data = new HashMap<>();
            data.put("simulationId", simulationId);
            data.put("qaId", qaId);
            data.put("originalName", file.getOriginalFilename());
            data.put("size", file.getSize());
            data.put("contentType", file.getContentType());
            data.put("transcript", transcript);
            data.put("feedback", feedback);

            return ApiResponse.builder().code(200).message("success").data(data).build();
        } catch (Exception e) {
            return ApiResponse.builder().code(500).message("upload_failed").data(Map.of(
                    "simulationId", simulationId,
                    "qaId", qaId,
                    "error", e.getMessage()
            )).build();
        }
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
    public ApiResponse<Object> finalizeSelection(
            @PathVariable Long simulationId,
            @AuthenticationPrincipal User user,
            @RequestBody @Valid FinalizeRequest request
    ) {
        if (user == null) {
            return ApiResponse.builder().code(401).message("로그인이 필요합니다.").build();
        }
        simulationService.ensureOwner(simulationId, user.getUserId());

        var finalList = simulationService.finalizeReplaceAndDelete(simulationId, request);

        var respQaList = finalList.stream().map(q ->
                QaDto.builder()
                        .qaId(q.getQaId())
                        .qaOrder(q.getQaOrder())
                        .qaQuestion(q.getQaQuestion())
                        .qaAnswer(q.getQaAnswer())
                        .build()
        ).toList();

        return ApiResponse.builder()
                .code(200)
                .message("success")
                .data(Map.of("qaList", respQaList))
                .build();
    }

    @PatchMapping("/{simulationId}/{qaCount}")
    public ApiResponse<?> stopSimulation(@PathVariable Long simulationId, @PathVariable Long qaCount){

        String message;

        if(simulationService.end(simulationId,qaCount)){
            message = "success";
            log.info("SUCCESS");
        } else {
            message = "fail";
            log.info("FAIL");
        }

        return ApiResponse.builder()
                .code(200)
                .message(message)
                .data("ok")
                .build();
    }

    @GetMapping("{simulationId}/transCheck")
    public ApiResponse<?> transCheck(@PathVariable Long simulationId) {

        boolean isOk = simulationService.transCheck(simulationId);

        return ApiResponse.builder()
                .code(200)
                .message("success")
                .data(isOk)
                .build();
    }
}
