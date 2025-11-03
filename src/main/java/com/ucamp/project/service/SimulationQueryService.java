package com.ucamp.project.service;

import com.ucamp.project.dto.*;
import com.ucamp.project.model.Transcription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimulationQueryService {
    private final SimulationService simulationService;
    private final TranscriptionService transcriptionService;

    @Transactional(readOnly = true)
    public SimulationResultDto buildResult(Long simulationId) {
        // 시뮬 상세 (PostDto + QaDto 리스트 포함해야 함)
        SimulationDetailResponse detail = simulationService.findDetail(simulationId);

        Map<Long, String> trMap = transcriptionService.findAllBySimulation(simulationId).stream()
                .collect(Collectors.toMap(t -> t.getQa().getQaId(), Transcription::getTrAnswerText));

        // detail.getPost().getQaList()는 QaDto 리스트라고 가정
        List<QaDto> mapped = detail.getPost().getQaList().stream()
                .map(qa -> QaDto.builder()
                        .qaId(qa.getQaId())
                        .qaOrder(qa.getQaOrder())
                        .qaQuestion(qa.getQaQuestion())
                        .qaAnswer(qa.getQaAnswer())
                        .transContent(trMap.getOrDefault(qa.getQaId(), "")) // 핵심
                        .build())
                .toList();

        PostDto post = PostDto.builder()
                .postId(detail.getPost().getPostId())
                .postTitle(detail.getPost().getPostTitle())
                .postDescription(detail.getPost().getPostDescription())
                .qaList(mapped)
                .build();

        return SimulationResultDto.builder()
                .simulationId(simulationId)
                .post(post)
                .build();
    }
}
